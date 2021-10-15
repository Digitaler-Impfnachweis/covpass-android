/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.countries

import com.ensody.reactivestate.dispatchers
import kotlinx.coroutines.invoke

public class CovPassCountriesLocalDataSource(
    private val covPasCountriesDao: CountriesDao
) {

    public suspend fun insertAll(countries: Collection<String>) {
        dispatchers.io {
            covPasCountriesDao.insertAll(countries)
        }
    }

    public suspend fun getAllCountries(): List<String> =
        dispatchers.io {
            covPasCountriesDao.getAll().map { it.countryCode }
        }
}
