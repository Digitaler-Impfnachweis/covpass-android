package com.ibm.health.vaccination.app.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.dependencies.sdkDeps
import com.ibm.health.vaccination.app.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.models.ExtendedVaccinationCertificate
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi

class MainState(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    @ExperimentalSerializationApi
    fun onQrContentReceived(qrContent: String) {
        launch {
            val vaccinationCertificateList = Storage.certCache.value
            vaccinationCertificateList.addCertificate(
                // FIXME replace validationQrContent with the simplified validation certificate
                ExtendedVaccinationCertificate(sdkDeps.qrCoder.decode(qrContent), qrContent, qrContent)
            )
            Storage.setVaccinationCertificateList(vaccinationCertificateList)
        }
    }
}
