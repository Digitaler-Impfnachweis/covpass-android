/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.common.ToggleFavoriteUseCase
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.coroutines.CoroutineScope

/**
 * ViewModel providing the [onFavoriteClick] function to mark a Certificate as favorite.
 */
internal class CertificateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = covpassDeps.toggleFavoriteUseCase,
) : BaseReactiveState<BaseEvents>(scope) {

    fun onFavoriteClick(certId: GroupedCertificatesId) {
        launch {
            toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }
}
