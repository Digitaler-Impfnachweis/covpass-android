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
 * Interface to communicate events from [ValidationQRScannerViewModel] to [ValidationQRScannerFragment].
 */
internal interface ValidationQRScannerEvents : BaseEvents {
    fun onValidationSuccess(certificate: CovCertificate)
    fun onValidationFailure()
    fun onImmunizationIncomplete(certificate: CovCertificate)
}

/**
 * ViewModel holding the business logic for decoding and validating a [CovCertificate].
 */
internal class ValidationQRScannerViewModel(scope: CoroutineScope) : BaseState<ValidationQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
                if (vaccinationCertificate.hasFullProtection) {
                    eventNotifier {
                        onValidationSuccess(vaccinationCertificate)
                    }
                } else {
                    eventNotifier {
                        onImmunizationIncomplete(vaccinationCertificate)
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
