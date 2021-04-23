package com.ibm.health.vaccination.app.vaccinee.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.common.AddCertUseCase
import com.ibm.health.vaccination.app.vaccinee.common.ToggleFavoriteUseCase
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

    fun onFavoriteClick(certId: String) {
        launch {
            ToggleFavoriteUseCase().toggleFavorite(certId)
        }
    }

    fun onQrContentReceived(qrContent: String) {
        launch {
            AddCertUseCase().addCertFromQr(qrContent)
        }
    }
}
