/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.getData
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.BlacklistedEntityFromFutureDateException
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [CovPassQRScannerViewModel] to [CovPassQRScannerFragment].
 */
internal interface CovPassQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: GroupedCertificatesId)
}

/**
 * ViewModel holding the business logic for decoding the [CovCertificate].
 */
internal class CovPassQRScannerViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    store: StateFlowStore,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val certRepository: CertRepository = covpassDeps.certRepository,
) : BaseReactiveState<CovPassQRScannerEvents>(scope) {

    val lastCertificateId by store.getData<GroupedCertificatesId?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val covCertificate = qrCoder.decodeCovCert(qrContent)
            var hasBeenBlacklisted = false
            try {
                validateEntity(covCertificate.dgcEntry.idWithoutPrefix)
            } catch (e: BlacklistedEntityFromFutureDateException) {
                hasBeenBlacklisted = true
            }
            val certsFlow = certRepository.certs
            var certId: GroupedCertificatesId? = null
            certsFlow.update {
                certId = it.addNewCertificate(
                    CombinedCovCertificate(
                        covCertificate = covCertificate,
                        qrContent = qrContent,
                        timestamp = System.currentTimeMillis(),
                        status = when {
                            covCertificate.isInExpiryPeriod() -> {
                                CertValidationResult.ExpiryPeriod
                            }
                            hasBeenBlacklisted -> {
                                CertValidationResult.ValidUntilDate
                            }
                            else -> {
                                CertValidationResult.Valid
                            }
                        },
                        hasSeenBoosterNotification = false,
                        hasSeenBoosterDetailNotification = false,
                        hasSeenBlacklistedNotification = false,
                        hasSeenExpiryNotification = false,
                        boosterNotificationRuleIds = mutableListOf(),
                    )
                )
            }
            certId?.let {
                lastCertificateId.value = it
                eventNotifier {
                    onScanSuccess(it)
                }
            }
        }
    }
}
