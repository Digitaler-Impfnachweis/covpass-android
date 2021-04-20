package com.ibm.health.vaccination.app.vaccinee.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.storage.Storage
import kotlinx.coroutines.CoroutineScope

interface DetailEvents : BaseEvents {
    fun onDeleteDone()
}

class DetailState(scope: CoroutineScope, val certId: String) : BaseState<DetailEvents>(scope) {

    fun onDelete() {
        launch {
            val vaccinationCertificateList = Storage.certCache.value
            vaccinationCertificateList.deleteCertificate(certId)
            Storage.setVaccinationCertificateList(vaccinationCertificateList)
            eventNotifier {
                onDeleteDone()
            }
        }
    }
}
