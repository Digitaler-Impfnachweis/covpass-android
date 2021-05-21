/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.BaseState
import de.rki.covpass.app.dependencies.covpassDeps
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onFavoriteClick] function to mark a Certificate as favorite.
 */
internal class CertificateViewModel(scope: CoroutineScope) : BaseState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: String) {
        launch {
            covpassDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
