package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.sdk.android.qr.models.CertAlreadyExistsException
import kotlinx.coroutines.CoroutineScope

interface ScannerEvents : BaseEvents {
    fun onScanFinished()
    fun onCertificateDuplicated()
}

class VaccinationScannerState(scope: CoroutineScope) : BaseState<ScannerEvents>(scope) {

    fun onQrContentReceived(qrContent: String) {
        launch {
            try {
                vaccineeDeps.addCertUseCase.addCertFromQr(qrContent)
                eventNotifier {
                    onScanFinished()
                }
            } catch (e: CertAlreadyExistsException) {
                eventNotifier {
                    onCertificateDuplicated()
                }
            }
        }
    }
}
