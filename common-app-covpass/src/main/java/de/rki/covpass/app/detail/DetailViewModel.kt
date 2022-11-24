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
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.FederalStateRepository
import de.rki.covpass.sdk.cert.CovPassMaskRulesDateResolver
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

internal interface DetailEvents<T> : BaseEvents {
    fun onHasSeenAllDetailNotificationUpdated(tag: T)
}

internal class DetailViewModel<T> @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = covpassDeps.toggleFavoriteUseCase,
    private val covPassMaskRulesDateResolver: CovPassMaskRulesDateResolver = sdkDeps.covPassMaskRulesDateResolver,
    private val federalStateRepository: FederalStateRepository = commonDeps.federalStateRepository,
) : BaseReactiveState<DetailEvents<T>>(scope) {

    val maskRuleValidFrom: MutableStateFlow<String?> = MutableStateFlow(null)

    private fun getRuleValidFromDate() {
        launch {
            maskRuleValidFrom.value = covPassMaskRulesDateResolver.getMaskRuleValidity(
                federalStateRepository.federalState.value.lowercase(),
            )
        }
    }

    init {
        getRuleValidFromDate()
    }

    fun onFavoriteClick(certId: GroupedCertificatesId) {
        launch {
            toggleFavoriteUseCase.toggleFavorite(certId)
        }
    }

    fun updateHasSeenAllDetailNotification(certId: GroupedCertificatesId, tag: T) {
        launch {
            covpassDependencies.certRepository.certs.update { groupedCertificateList ->
                groupedCertificateList.certificates.find { it.id == certId }?.let {
                    if (it.boosterNotification.result == BoosterResult.Passed) {
                        it.hasSeenBoosterDetailNotification = true
                    }
                    if (it.isBoosterReadyForReissue() || it.isExpiredReadyForReissue()) {
                        it.hasSeenReissueDetailNotification = true
                    }
                }
            }
            eventNotifier { onHasSeenAllDetailNotificationUpdated(tag) }
        }
    }
}
