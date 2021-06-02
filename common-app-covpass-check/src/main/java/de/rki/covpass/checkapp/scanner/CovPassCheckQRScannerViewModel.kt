/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.ExpiredCwtException
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [CovPassCheckQRScannerViewModel] to [CovPassCheckQRScannerFragment].
 */
internal interface CovPassCheckQRScannerEvents : BaseEvents {
    fun onValidationSuccess(certificate: CovCertificate)
    fun onValidationFailure()
    fun onImmunizationIncomplete(certificate: CovCertificate)
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class CovPassCheckQRScannerViewModel(scope: CoroutineScope) : BaseState<CovPassCheckQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val covCertificate = sdkDeps.qrCoder.decodeCovCert(qrContent)
                if (covCertificate.hasFullProtection) {
                    eventNotifier {
                        onValidationSuccess(covCertificate)
                    }
                } else {
                    eventNotifier {
                        onImmunizationIncomplete(covCertificate)
                    }
                }
            } catch (exception: Exception) {
                when (exception) {
                    is BadCoseSignatureException, is ExpiredCwtException -> {
                        Lumber.e(exception)
                        eventNotifier {
                            onValidationFailure()
                        }
                    }
                    else -> {
                        throw exception
                    }
                }
            }
        }
    }
}
