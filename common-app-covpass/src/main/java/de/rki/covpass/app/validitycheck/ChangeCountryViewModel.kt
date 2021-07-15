/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryRepository
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.DefaultCovPassRulesRepository
import kotlinx.coroutines.CoroutineScope

internal class ChangeCountryViewModel(
    scope: CoroutineScope,
    private val rulesRepository: DefaultCovPassRulesRepository = sdkDeps.rulesRepository
) : BaseReactiveState<BaseEvents>(scope) {

    val countryList: MutableValueFlow<List<Country>> = MutableValueFlow(emptyList())

    fun getCountryCodes() {
        launch {
            countryList.value = rulesRepository.getAllCountryCodes().map {
                CountryRepository.getCountryByCode(it)
            }
        }
    }
}
