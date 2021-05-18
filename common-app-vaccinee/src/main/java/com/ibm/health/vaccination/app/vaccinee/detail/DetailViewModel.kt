package com.ibm.health.vaccination.app.vaccinee.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import kotlinx.coroutines.CoroutineScope

internal interface DetailEvents : BaseEvents {
    fun onDeleteDone()
}

/**
 * ViewModel providing the [onDelete] and [onFavoriteClick] functionality.
 */
internal class DetailViewModel(scope: CoroutineScope, private val certId: String) : BaseState<DetailEvents>(scope) {
    fun onDelete() {
        launch {
            vaccineeDeps.certRepository.certs.update {
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
}
