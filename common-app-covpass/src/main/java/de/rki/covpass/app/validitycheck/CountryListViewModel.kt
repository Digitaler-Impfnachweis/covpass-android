/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import com.ensody.reactivestate.BaseReactiveState
import com.ensody.reactivestate.DependencyAccessor
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.BaseEvents
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryResolver
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassCountriesRepository
import kotlinx.coroutines.CoroutineScope

internal class CountryListViewModel @OptIn(DependencyAccessor::class) constructor(
    scope: CoroutineScope,
    private val countriesRepository: CovPassCountriesRepository = sdkDeps.covPassCountriesRepository,
) : BaseReactiveState<BaseEvents>(scope) {

    val countries: MutableValueFlow<List<Country>> = MutableValueFlow(emptyList())

    init {
        getSortedCountries()
    }

    private fun getSortedCountries() {
        launch {
            countries.value = CountryResolver.getSortedCountryList(
                countriesRepository.getAllCovPassCountries()
            )
        }
    }
}
