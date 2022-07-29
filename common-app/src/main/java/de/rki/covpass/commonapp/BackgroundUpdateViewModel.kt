/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.revocation.isBeforeUpdateInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@SuppressWarnings("UnusedPrivateMember")
public open class BackgroundUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BaseReactiveState<BaseEvents>(scope) {

    private val backgroundDscListUpdater: Updater = Updater {
        if (sdkDependencies.dscRepository.lastUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.dscListUpdater.update()
        }
    }
    private val valueSetsUpdater: Updater = Updater {
        if (sdkDependencies.rulesUpdateRepository.lastValueSetsUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassValueSetsRepository.loadValueSets()
        }
    }

    public open fun update() {
        backgroundDscListUpdater.update()
        valueSetsUpdater.update()
    }

    protected fun logOnError(throwable: Throwable) {
        Lumber.e(throwable)
    }

    protected inner class Updater(private var block: suspend CoroutineScope.() -> Unit) {
        private var job: Job? = null

        public fun update() {
            if (job?.isActive != true) {
                job = launch(onError = ::logOnError, withLoading = null, block = block)
            }
        }
    }
}
