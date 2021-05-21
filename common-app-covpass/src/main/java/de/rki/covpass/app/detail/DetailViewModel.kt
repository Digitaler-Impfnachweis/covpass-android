/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
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
            covpassDeps.certRepository.certs.update {
                newMainCertId = it.deleteVaccinationCertificate(certId)
            }
            eventNotifier {
                onDeleteDone(newMainCertId)
            }
        }
    }

    fun onFavoriteClick(certId: String) {
        launch {
            covpassDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
