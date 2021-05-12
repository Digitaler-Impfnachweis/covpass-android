package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ensody.reactivestate.StateFlowStore
import com.ensody.reactivestate.getData
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.sdk.android.cert.models.CombinedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

internal interface VaccinationQRScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: String)
}

internal class VaccinationQRScannerState(
    scope: CoroutineScope,
    store: StateFlowStore,
) : BaseState<VaccinationQRScannerEvents>(scope) {

    val lastCertificateId by store.getData<String?>(null)

    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
            lastCertificateId.value = vaccinationCertificate.vaccination.id
            vaccineeDeps.certRepository.certs.update {
                it.addCertificate(
                    CombinedVaccinationCertificate(vaccinationCertificate, qrContent)
                )
            }
            eventNotifier {
                onScanSuccess(
                    vaccinationCertificate.vaccination.id
                )
            }
        }
    }
}
