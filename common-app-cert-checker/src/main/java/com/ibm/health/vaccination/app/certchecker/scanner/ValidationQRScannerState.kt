package com.ibm.health.vaccination.app.certchecker.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.logging.Lumber
import com.ibm.health.vaccination.sdk.android.cert.BadCoseSignatureException
import com.ibm.health.vaccination.sdk.android.cert.ExpiredCwtException
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

internal interface ValidationQRScannerEvents : BaseEvents {
    fun onValidationSuccess(certificate: VaccinationCertificate)
    fun onValidationFailure()
    fun onImmunizationIncomplete(certificate: VaccinationCertificate)
}

internal class ValidationQRScannerState(scope: CoroutineScope) : BaseState<ValidationQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val validationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
                if (validationCertificate.hasFullProtection) {
                    eventNotifier {
                        onValidationSuccess(validationCertificate)
                    }
                } else {
                    eventNotifier {
                        onImmunizationIncomplete(validationCertificate)
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
                    // TODO this case should be handled more differentiated later
                    else -> {
                        Lumber.e(exception)
                        eventNotifier {
                            onValidationFailure()
                        }
                    }
                }
            }
        }
    }
}
