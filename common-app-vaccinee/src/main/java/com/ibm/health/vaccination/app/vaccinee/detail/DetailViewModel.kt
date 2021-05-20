package com.ibm.health.vaccination.app.vaccinee.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [DetailViewModel] to [DetailFragment].
 */
internal interface DetailEvents : BaseEvents {
    fun onDeleteDone(newMainCertId: String?)
}

/**
 * ViewModel providing the [onDelete] and [onFavoriteClick] functionality.
 */
internal class DetailViewModel(scope: CoroutineScope) : BaseState<DetailEvents>(scope) {

    fun onDelete(certId: String) {
        launch {
            var newMainCertId: String? = null
            vaccineeDeps.certRepository.certs.update {
                newMainCertId = it.deleteVaccinationCertificate(certId)
            }
            eventNotifier {
                onDeleteDone(newMainCertId)
            }
        }
    }

    fun onFavoriteClick(certId: String) {
        launch {
            vaccineeDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
