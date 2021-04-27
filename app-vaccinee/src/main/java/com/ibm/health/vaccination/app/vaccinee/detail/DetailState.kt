package com.ibm.health.vaccination.app.vaccinee.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import kotlinx.coroutines.CoroutineScope

interface DetailEvents : BaseEvents {
    fun onDeleteDone()
}

class DetailState(scope: CoroutineScope, val certId: String) : BaseState<DetailEvents>(scope) {
    fun onDelete() {
        launch {
            vaccineeDeps.storage.certs.update {
                it.deleteCertificate(certId)
            }
            eventNotifier {
                onDeleteDone()
            }
        }
    }

    fun onFavoriteClick(certId: String) {
        launch {
            vaccineeDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }

    fun onQrContentReceived(qrContent: String) {
        launch {
            vaccineeDeps.addCertUseCase.addCertFromQr(qrContent)
        }
    }
}
