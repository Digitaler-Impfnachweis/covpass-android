/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.coroutines.CoroutineScope

internal interface ReissueNotificationEvents : BaseEvents {
    fun onUpdateHasSeenReissueNotificationFinish(continueReissue: Boolean)
}

internal class ReissueNotificationViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val listCertIds: List<String>,
    private val certRepository: CertRepository = covpassDeps.certRepository,
) : BaseReactiveState<ReissueNotificationEvents>(scope) {

    fun updateHasSeenReissueNotification(continueReissue: Boolean) {
        launch {
            certRepository.certs.update {
                it.certificates.forEach { groupedCertificates ->
                    if (
                        groupedCertificates.certificates.any { combinedCovCertificate ->
                            listCertIds.contains(combinedCovCertificate.covCertificate.dgcEntry.id)
                        }
                    ) {
                        groupedCertificates.hasSeenReissueNotification = true
                    }
                }
            }
            eventNotifier {
                onUpdateHasSeenReissueNotificationFinish(continueReissue)
            }
        }
    }
}
