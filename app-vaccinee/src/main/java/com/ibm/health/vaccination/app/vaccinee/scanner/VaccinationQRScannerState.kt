package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.getData
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.vaccination.app.errorhandling.isConnectionError
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import io.ktor.client.features.*
import kotlinx.coroutines.CoroutineScope

interface VaccinationQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: String)
}

class VaccinationQRScannerState(
    scope: CoroutineScope,
    store: StateFlowStore,
) : BaseState<VaccinationQRScannerEvents>(scope) {

    val lastCertificateId by store.getData<String?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
            lastCertificateId.value = vaccinationCertificate.id
            var connectionError: Throwable? = null
            var validationCertContent: String? = null
            try {
                validationCertContent = sdkDeps.certService.getValidationCert(qrContent)
            } catch (error: Throwable) {
                when {
                    isConnectionError(error) -> connectionError = error
                    else -> throw error
                }
            }
            vaccineeDeps.storage.certs.update {
                it.addCertificate(
                    ExtendedVaccinationCertificate(vaccinationCertificate, qrContent, validationCertContent)
                )
            }
            if (connectionError != null) {
                eventNotifier { onError(connectionError) }
            } else {
                eventNotifier { onScanSuccess(vaccinationCertificate.id) }
            }
        }
    }
}
