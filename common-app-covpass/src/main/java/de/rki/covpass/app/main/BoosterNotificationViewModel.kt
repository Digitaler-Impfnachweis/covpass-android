/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.dependencies.CovpassDependencies
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.sdk.cert.models.BoosterResult
import kotlinx.coroutines.CoroutineScope

internal interface BoosterNotificationEvents : BaseEvents {
    fun onHasSeenBoosterNotificationUpdated()
}

internal class BoosterNotificationViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
) : BaseReactiveState<BoosterNotificationEvents>(scope) {

    fun updateHasSeenBoosterNotification() {
        launch {
            covpassDependencies.certRepository.certs.update { groupedCertificateList ->
                groupedCertificateList.certificates.forEach {
                    if (it.boosterResult == BoosterResult.Passed) {
                        it.hasSeenBoosterNotification = true
                    }
                }
            }
            eventNotifier { onHasSeenBoosterNotificationUpdated() }
        }
    }
}
