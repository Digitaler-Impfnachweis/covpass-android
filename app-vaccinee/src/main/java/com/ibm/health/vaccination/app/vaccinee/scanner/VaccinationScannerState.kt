package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.sdk.android.qr.models.CertAlreadyExistsException
import com.ibm.health.vaccination.sdk.android.qr.models.ExtendedVaccinationCertificate
import kotlinx.coroutines.CoroutineScope

interface ScannerEvents : BaseEvents {
    fun onScanSuccess(certificateId: String)
    fun onCertificateDuplicated()
}

class VaccinationScannerState(scope: CoroutineScope) : BaseState<ScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                val vaccinationCertificate = sdkDeps.qrCoder.decodeVaccinationCert(qrContent)
                vaccineeDeps.storage.certs.update {
                    it.addCertificate(
                        // FIXME replace validationQrContent with the simplified validation certificate
                        ExtendedVaccinationCertificate(vaccinationCertificate, qrContent, qrContent)
                    )
                }
                eventNotifier {
                    onScanSuccess(vaccinationCertificate.id)
                }
            } catch (e: CertAlreadyExistsException) {
                eventNotifier {
                    onCertificateDuplicated()
                }
            }
        }
    }
}
