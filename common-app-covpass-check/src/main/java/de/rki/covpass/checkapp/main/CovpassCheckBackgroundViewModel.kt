/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.commonapp.BackgroundUpdateViewModel
import de.rki.covpass.commonapp.isBeforeUpdateInterval
import de.rki.covpass.sdk.dependencies.SdkDependencies
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.coroutines.CoroutineScope

public class CovpassCheckBackgroundViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val sdkDependencies: SdkDependencies = sdkDeps,
) : BackgroundUpdateViewModel(scope, sdkDependencies) {

    private val euRulesUpdater: Updater = Updater {
        if (sdkDependencies.rulesUpdateRepository.lastEuRulesUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassEuRulesRepository.loadRules()
        }
    }
    private val domesticRulesUpdater: Updater = Updater {
        if (sdkDependencies.rulesUpdateRepository.lastDomesticRulesUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassDomesticRulesRepository.loadRules()
        }
    }
    private val countryListUpdater: Updater = Updater {
        if (sdkDependencies.rulesUpdateRepository.lastCountryListUpdate.value.isBeforeUpdateInterval()) {
            sdkDependencies.covPassCountriesRepository.loadCountries()
        }
    }

    public override fun update() {
        super.update()
        euRulesUpdater.update()
        domesticRulesUpdater.update()
        countryListUpdater.update()
    }
}
