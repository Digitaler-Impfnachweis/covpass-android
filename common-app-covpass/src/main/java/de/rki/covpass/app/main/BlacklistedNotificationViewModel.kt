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
import kotlinx.coroutines.CoroutineScope

internal interface BlacklistedNotificationEvents : BaseEvents {
    fun onUpdatedBlacklistedNotification()
}

internal class BlacklistedNotificationViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val covpassDependencies: CovpassDependencies = covpassDeps,
) : BaseReactiveState<BlacklistedNotificationEvents>(scope) {

    fun updateHasSeenBlacklistedNotification() {
        launch {
            covpassDependencies.certRepository.certs.update { groupedCertificateList ->
                groupedCertificateList.certificates.forEach {
                    if (it.hasBeenBlacklisted) {
                        it.hasSeenBlacklistedNotification = true
                    }
                }
            }
            eventNotifier {
                onUpdatedBlacklistedNotification()
            }
        }
    }
}
