/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.misuseprevention.MisusePreventionHelper
import de.rki.covpass.app.misuseprevention.MisusePreventionStatus
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.validateEntity
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import de.rki.covpass.sdk.revocation.validateRevocation
import de.rki.covpass.sdk.storage.CertRepository
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerializationException

/**
 * Interface to communicate events from [CovPassQRScannerViewModel] to [CovPassQRScannerFragment].
 */
internal interface CovPassQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: GroupedCertificatesId)
    fun onLimitationWarning(qrContent: String)
    fun onTicketingQrcodeScan(ticketingDataInitialization: TicketingDataInitialization)
}

/**
 * ViewModel holding the business logic for decoding the [CovCertificate].
 */
internal class CovPassQRScannerViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val qrCoder: QRCoder = sdkDeps.qrCoder,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val revocationRemoteListRepository: RevocationRemoteListRepository = sdkDeps.revocationRemoteListRepository,
) : BaseReactiveState<CovPassQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val data = qrCoder.validateTicketing(qrContent)
                eventNotifier {
                    onTicketingQrcodeScan(data)
                }
            } catch (e: SerializationException) {
                val covCertificate = qrCoder.decodeCovCert(qrContent, allowExpiredCertificates = true)
                validateEntity(covCertificate.dgcEntry.idWithoutPrefix)
                if (validateRevocation(covCertificate, revocationRemoteListRepository)) {
                    if (covCertificate.isGermanCertificate) {
                        throw RevokedCertificateGermanCertificateException()
                    } else {
                        throw RevokedCertificateNotGermanCertificateException()
                    }
                }
                validateMisusePrevention(
                    certRepository.certs.value.certificates,
                    covCertificate,
                    qrContent,
                )
            }
        }
    }

    private fun validateMisusePrevention(
        groupedCertificates: List<GroupedCertificates>,
        covCertificate: CovCertificate,
        qrContent: String,
    ) {
        when (MisusePreventionHelper.getMisusePreventionStatus(groupedCertificates, covCertificate)) {
            MisusePreventionStatus.ALL_GOOD -> {
                addNewCertificate(covCertificate, qrContent)
            }
            MisusePreventionStatus.FIRST_LIMITATION_WARNING,
            MisusePreventionStatus.SECOND_LIMITATION_WARNING,
            -> {
                eventNotifier { onLimitationWarning(qrContent) }
            }
            MisusePreventionStatus.SAVING_BLOCKED -> {
                throw SavingBlockedException()
            }
        }
    }

    private fun addNewCertificate(covCertificate: CovCertificate, qrContent: String) {
        launch {
            CovPassCertificateStorageHelper.addNewCertificate(
                certRepository.certs,
                covCertificate,
                qrContent,
            )?.let {
                eventNotifier {
                    onScanSuccess(it)
                }
            }
        }
    }
}

public class SavingBlockedException : IllegalStateException()

public class RevokedCertificateGermanCertificateException : IllegalStateException()

public class RevokedCertificateNotGermanCertificateException : IllegalStateException()
