package com.ibm.health.vaccination.app.certchecker.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.logging.Lumber
import com.ibm.health.vaccination.sdk.android.cert.HCertBadSignatureException
import com.ibm.health.vaccination.sdk.android.cert.HCertExpiredException
import com.ibm.health.vaccination.sdk.android.cert.models.ValidationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

interface ValidationQRScannerEvents : BaseEvents {
    fun onValidationSuccess(certificate: ValidationCertificate)
    fun onValidationFailure()
    fun onImmunizationIncomplete(certificate: ValidationCertificate)
}

class ValidationQRScannerState(scope: CoroutineScope) : BaseState<ValidationQRScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val validationCertificate = sdkDeps.qrCoder.decodeValidationCert(qrContent)
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
                    is HCertBadSignatureException, is HCertExpiredException -> {
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
