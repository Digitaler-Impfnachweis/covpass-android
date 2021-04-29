package com.ibm.health.vaccination.app.certchecker.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import com.ibm.health.vaccination.sdk.android.qr.HCertBadSignatureException
import com.ibm.health.vaccination.sdk.android.qr.HCertExpiredException
import com.ibm.health.vaccination.sdk.android.qr.models.ValidationCertificate
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
                if (validationCertificate.isComplete) {
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
                        eventNotifier {
                            onValidationFailure()
                        }
                    }
                    else -> throw exception
                }
            }
        }
    }
}
