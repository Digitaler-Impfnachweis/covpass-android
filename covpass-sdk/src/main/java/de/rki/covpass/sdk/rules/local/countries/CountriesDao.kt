/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.countries

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
public abstract class CountriesDao {

    @Query("SELECT * from countries")
    public abstract suspend fun getAll(): List<CountryLocal>

    @Transaction
    public open suspend fun insertAll(countries: Collection<String>) {
        deleteAll()
        countries.forEach {
            insert(CountryLocal(countryCode = it))
        }
    }

    @Insert
    public abstract suspend fun insert(country: CountryLocal)

    @Query("DELETE FROM countries")
    public abstract suspend fun deleteAll()
}
