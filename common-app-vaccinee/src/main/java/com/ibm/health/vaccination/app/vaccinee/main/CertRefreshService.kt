package com.ibm.health.vaccination.app.vaccinee.main

import com.ensody.reactivestate.*
import com.ibm.health.common.logging.Lumber
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificates
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import com.ibm.health.vaccination.sdk.android.cert.CertService
import com.ibm.health.vaccination.sdk.android.utils.ExponentialBackoffRetryStrategy
import com.ibm.health.vaccination.sdk.android.utils.parallelMap
import com.ibm.health.vaccination.sdk.android.utils.retry
import io.ktor.client.features.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.time.Instant

/**
 * Service for retrying vaccination cert to validation cert exchange with the backend.
 * The service is triggered by any changes in the data changes, therefore it collects events from the [certs].
 * It can also be triggered manually by [triggerUpdate].
 * As long as there are missing validation certs, the service will retry exchanging them.
 * If the reason for the missing cert is something non-fatal like no internet, the retry is done often with exponential
 * backoff.
 * If the reason for the missing cert is something fatal like a 404 error, the retry is done only sporadic, because
 * there is not a high chance of success and the backend connection should not be stressed unnecessarily.
 */
internal class CertRefreshService(
    private val scope: CoroutineScope,
    private val certService: CertService,
    private val certs: SuspendMutableValueFlow<GroupedCertificatesList>,
) {

    /** The set of certificate IDs that have a fatal problem which we might indicate to the user. */
    val failingCertIds: StateFlow<Set<String>> by lazy {
        scope.derived { get(certIdsWithFatalErrors).keys }
    }

    private val triggers = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    private val certIdsWithFatalErrors = MutableValueFlow(mapOf<String, Instant>())

    init {
        scope.launch {
            certs.collect { triggerUpdate() }
        }
        scope.launch {
            while (true) {
                withErrorReporting({ Lumber.e(it) }) {
                    triggers.receive()

                    retry(retryStrategy = TriggeredExponentialBackoff()) {
                        refresh()
                    }
                }
            }
        }
    }

    fun triggerUpdate() {
        triggers.offer(Unit)
    }

    private suspend fun refresh() {
        // Remove gone certs from errors set
        val certIds = certs.value.certificates.map { it.getMainCertId() }.toSet()
        val yesterday = Instant.now().minusSeconds(24 * 60 * 60)
        certIdsWithFatalErrors.value = certIdsWithFatalErrors.value.filter { (certId, lastRetry) ->
            certId in certIds && lastRetry.isAfter(yesterday)
        }

        parallelMap(certs.value.certificates) {
            updateCertificate(it)
        }
    }

    private suspend fun updateCertificate(cert: GroupedCertificates) {
        // TODO: Also refresh certs that will become invalid in less than 3 days
        if (cert.getMainCertificate().validationQrContent != null ||
            cert.getMainCertId() in certIdsWithFatalErrors.value
        ) {
            return
        }

        val vaccinationQrContent = cert.getMainCertificate().vaccinationQrContent
        try {
            val validationCertContent = certService.getValidationCert(vaccinationQrContent)
            certs.update {
                it.addValidationQrContent(cert.getMainCertId(), validationCertContent)
            }
        } catch (e: ClientRequestException) {
            certIdsWithFatalErrors.value += cert.getMainCertId() to Instant.now()
        }
    }

    private inner class TriggeredExponentialBackoff : ExponentialBackoffRetryStrategy() {
        override suspend fun delayRetry() {
            select<Unit> {
                triggers.onReceive { resetDelay() }
                scope.launch { delay(delay) }.onJoin { increaseDelay() }
            }
        }
    }
}
