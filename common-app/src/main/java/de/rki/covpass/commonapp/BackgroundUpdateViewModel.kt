/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.toTrustedCerts
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import java.time.temporal.ChronoUnit

public open class BackgroundUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BaseReactiveState<BaseEvents>(scope) {

    init {
        fetchDscList()
        fetchRules()
        fetchValueSets()
    }

    protected fun logOnError(throwable: Throwable) {
        Lumber.e(throwable)
    }

    private fun fetchDscList() {
        launch(onError = ::logOnError, withLoading = null) {
            if (sdkDependencies.dscRepository.lastUpdate.value.isBeforeUpdateInterval()) {
                val result = sdkDependencies.dscListService.getTrustedList()
                val dscList = sdkDependencies.decoder.decodeDscList(result)
                sdkDependencies.validator.updateTrustedCerts(dscList.toTrustedCerts())
                sdkDependencies.dscRepository.updateDscList(dscList)
            }
        }
    }

    private fun fetchRules() {
        launch(onError = ::logOnError, withLoading = null) {
            if (sdkDependencies.rulesUpdateRepository.lastRulesUpdate.value.isBeforeUpdateInterval()) {
                sdkDependencies.covPassRulesRepository.loadRules()
            }
        }
    }

    private fun fetchValueSets() {
        launch(onError = ::logOnError, withLoading = null) {
            if (sdkDependencies.rulesUpdateRepository.lastValueSetsUpdate.value.isBeforeUpdateInterval()) {
                sdkDependencies.covPassValueSetsRepository.loadValueSets()
            }
        }
    }

    public companion object {
        public const val UPDATE_INTERVAL_HOURS: Long = 24
    }
}

public fun Instant.isBeforeUpdateInterval(): Boolean {
    return isBefore(Instant.now().minus(BackgroundUpdateViewModel.UPDATE_INTERVAL_HOURS, ChronoUnit.HOURS))
}
