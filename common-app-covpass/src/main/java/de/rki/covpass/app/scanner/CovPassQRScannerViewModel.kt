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
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException

/**
 * Interface to communicate events from [CovPassQRScannerViewModel] to [CovPassQRScannerFragment].
 */
internal interface CovPassQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: GroupedCertificatesId)
    fun onTicketingQrcodeScan(ticketingDataInitialization: TicketingDataInitialization)
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
            try {
                val data = qrCoder.validateTicketing(qrContent)
                eventNotifier {
                    onTicketingQrcodeScan(data)
                }
            } catch (e: SerializationException) {
                val covCertificate = qrCoder.decodeCovCert(qrContent)
                validateEntity(covCertificate.dgcEntry.idWithoutPrefix)
                val certsFlow = certRepository.certs
                var certId: GroupedCertificatesId? = null
                certsFlow.update {
                    certId = it.addNewCertificate(
                        CombinedCovCertificate(
                            covCertificate = covCertificate,
                            qrContent = qrContent,
                            timestamp = System.currentTimeMillis(),
                            status = if (covCertificate.isInExpiryPeriod()) {
                                CertValidationResult.ExpiryPeriod
                            } else {
                                CertValidationResult.Valid
                            },
                            hasSeenBoosterNotification = false,
                            hasSeenBoosterDetailNotification = false,
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
}
