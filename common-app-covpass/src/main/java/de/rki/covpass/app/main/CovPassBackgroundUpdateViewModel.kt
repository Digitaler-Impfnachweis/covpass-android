/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.commonapp.BackgroundUpdateViewModel
import de.rki.covpass.commonapp.isBeforeUpdateInterval
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

public class CovPassBackgroundUpdateViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BackgroundUpdateViewModel(scope, sdkDependencies) {

    init {
        fetchCountryList()
        fetchBoosterRules()
    }

    private fun fetchCountryList() {
        launch(onError = ::logOnError, withLoading = null) {
            if (sdkDependencies.rulesUpdateRepository.lastCountryListUpdate.value.isBeforeUpdateInterval()) {
                sdkDependencies.covPassCountriesRepository.loadCountries()
            }
        }
    }

    private fun fetchBoosterRules() {
        launch(onError = ::logOnError, withLoading = null) {
            if (sdkDependencies.rulesUpdateRepository.lastBoosterRulesUpdate.value.isBeforeUpdateInterval()) {
                sdkDependencies.covPassBoosterRulesRepository.loadBoosterRules()
            }
        }
    }
}
