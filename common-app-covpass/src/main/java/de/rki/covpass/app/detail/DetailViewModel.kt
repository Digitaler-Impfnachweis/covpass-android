/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.common.ToggleFavoriteUseCase
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.coroutines.CoroutineScope

internal interface DetailEvents<T> : BaseEvents {
    fun onHasSeenBoosterDetailNotificationUpdated(tag: T)
}

internal class DetailViewModel<T> @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = covpassDeps.toggleFavoriteUseCase,
) : BaseReactiveState<DetailEvents<T>>(scope) {

    fun onFavoriteClick(certId: GroupedCertificatesId) {
        launch {
            toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }

    fun updateHasSeenBoosterDetailNotification(certId: GroupedCertificatesId, tag: T) {
        launch {
            covpassDependencies.certRepository.certs.update { groupedCertificateList ->
                groupedCertificateList.certificates.find { it.id == certId }?.let {
                    if (it.boosterResult == BoosterResult.Passed) {
                        it.hasSeenBoosterDetailNotification = true
                    }
                }
            }
            eventNotifier { onHasSeenBoosterDetailNotificationUpdated(tag) }
        }
    }
}
