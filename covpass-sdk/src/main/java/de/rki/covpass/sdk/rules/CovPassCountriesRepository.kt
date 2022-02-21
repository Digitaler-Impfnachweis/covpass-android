/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassCountriesRemoteDataSource
import de.rki.covpass.sdk.rules.local.countries.CovPassCountriesLocalDataSource
import de.rki.covpass.sdk.storage.RulesUpdateRepository

public class CovPassCountriesRepository(
    private val remoteDataSource: CovPassCountriesRemoteDataSource,
    private val localDataSource: CovPassCountriesLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository,
) {

    public suspend fun getAllCovPassCountries(): List<String> {
        return localDataSource.getAllCountries()
    }

    public suspend fun prepopulate(countries: List<String>) {
        localDataSource.insertAll(countries)
    }

    public suspend fun loadCountries() {
        localDataSource.insertAll(remoteDataSource.getCountries())
        rulesUpdateRepository.markCountryListUpdated()
    }

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
