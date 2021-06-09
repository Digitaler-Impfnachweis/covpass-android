/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onFavoriteClick] function to mark a Certificate as favorite.
 */
internal class CertificateViewModel(scope: CoroutineScope) : BaseReactiveState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: GroupedCertificatesId) {
        launch {
            covpassDeps.toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
