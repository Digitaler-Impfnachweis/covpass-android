/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.coroutines.CoroutineScope

/**
 * Interface to communicate events from [DetailViewModel] to [DetailFragment].
 */
internal interface DetailEvents : BaseEvents {
    fun onDeleteDone(isGroupedCertDeleted: Boolean)
}

/**
 * ViewModel providing the [onDelete] and [onFavoriteClick] functionality.
 */
internal class DetailViewModel(scope: CoroutineScope) : BaseState<DetailEvents>(scope) {

    fun onDelete(certId: String) {
        launch {
            var isGroupedCertDeleted = false
            covpassDeps.certRepository.certs.update {
                isGroupedCertDeleted = it.deleteCovCertificate(certId)
            }
            eventNotifier {
                onDeleteDone(isGroupedCertDeleted)
            }
        }
    }

    fun onFavoriteClick(certId: GroupedCertificatesId) {
        launch {
            covpassDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
