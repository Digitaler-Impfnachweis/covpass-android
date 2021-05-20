/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.certchecker.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.logging.Lumber
import com.ibm.health.vaccination.sdk.android.cert.BadCoseSignatureException
import com.ibm.health.vaccination.sdk.android.cert.ExpiredCwtException
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [ValidationQRScannerViewModel] to [ValidationQRScannerFragment].
 */
internal interface ValidationQRScannerEvents : BaseEvents {
    fun onValidationSuccess(certificate: VaccinationCertificate)
    fun onValidationFailure()
    fun onImmunizationIncomplete(certificate: VaccinationCertificate)
}

/**
 * ViewModel holding the business logic for decoding and validating a [VaccinationCertificate].
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
