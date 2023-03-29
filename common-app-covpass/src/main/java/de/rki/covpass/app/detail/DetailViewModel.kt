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
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.ReissueState
import de.rki.covpass.sdk.cert.models.ReissueType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

internal interface DetailEvents<T> : BaseEvents {
    fun onHasSeenAllDetailNotificationUpdated(tag: T)
    fun onOpenReissue(reissueType: ReissueType, listCertIds: List<String>)
    fun onExpiredNonGermanReissuePopUp()
}

internal class DetailViewModel<T> @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val groupedCertificatesId: GroupedCertificatesId,
    isFirstAdded: Boolean,
    private val certId: String? = null,
    private val certRepository: CertRepository = covpassDeps.certRepository,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = covpassDeps.toggleFavoriteUseCase,
) : BaseReactiveState<DetailEvents<T>>(scope) {

    private fun validateReissue() {
        val groupedCertificate =
            certRepository.certs.value.getGroupedCertificates(groupedCertificatesId)
        groupedCertificate?.validateExpiredReissue()
        certId?.let { id ->
            val combinedCovCertificate = groupedCertificate?.certificates?.firstOrNull {
                it.covCertificate.dgcEntry.id == id
            }
            combinedCovCertificate?.let {
                if (it.reissueState == ReissueState.Ready) {
                    eventNotifier {
                        onOpenReissue(
                            it.reissueType,
                            listOf(certId) + groupedCertificate.getHistoricalDataForDcc(certId),
                        )
                    }
                }
            }
        }
    }

    private fun validateExpiredNonGermanReissue() {
        val groupedCertificate =
            certRepository.certs.value.getGroupedCertificates(groupedCertificatesId)
        certId?.let { id ->
            val combinedCovCertificate = groupedCertificate?.certificates?.firstOrNull {
                it.covCertificate.dgcEntry.id == id
            }
            combinedCovCertificate?.let {
                if (it.isExpiredAndNonGermanVacOrRec()) {
                    eventNotifier {
                        onExpiredNonGermanReissuePopUp()
                    }
                }
            }
        }
    }

    private fun CombinedCovCertificate.isExpiredAndNonGermanVacOrRec(): Boolean =
        status == CertValidationResult.Expired && !covCertificate.isGermanCertificate &&
            (covCertificate.dgcEntry is Vaccination || covCertificate.dgcEntry is Recovery)

    init {
        if (isFirstAdded) {
            validateReissue()
            validateExpiredNonGermanReissue()
        }
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
