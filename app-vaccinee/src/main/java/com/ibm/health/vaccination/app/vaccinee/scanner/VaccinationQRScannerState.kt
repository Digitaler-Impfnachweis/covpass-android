package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.getData
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.common.http.httpConfig
import com.ibm.health.common.vaccination.app.errorhandling.isConnectionError
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope

interface VaccinationQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: String)
}

class VaccinationQRScannerState(
    scope: CoroutineScope,
    store: StateFlowStore,
) : BaseState<VaccinationQRScannerEvents>(scope) {

    // FIXME just a intermediate test setup, switch to real cert request
    private val client = httpConfig.ktorClient().config {
        defaultRequest {
            host = "www.google.de"
        }
    }

    val lastCertificateId by store.getData<String?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
            lastCertificateId.value = vaccinationCertificate.id
            var connectionError: Throwable? = null
            try {
                // FIXME just a intermediate test setup, switch to real cert request
                client.get<HttpResponse>()
            } catch (error: Throwable) {
                if (isConnectionError(error)) {
                    connectionError = error
                } else {
                    throw error
                }
            }
            vaccineeDeps.storage.certs.update {
                it.addCertificate(
                    // FIXME replace validationQrContent with the simplified validation certificate
                    ExtendedVaccinationCertificate(vaccinationCertificate, qrContent, qrContent)
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
