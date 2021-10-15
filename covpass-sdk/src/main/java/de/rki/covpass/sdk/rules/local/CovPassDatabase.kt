/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.covpass.sdk.rules.booster.local.BoosterDescriptionLocal
import de.rki.covpass.sdk.rules.booster.local.BoosterRuleLocal
import de.rki.covpass.sdk.rules.booster.local.BoosterRulesDao
import de.rki.covpass.sdk.rules.local.countries.CountriesDao
import de.rki.covpass.sdk.rules.local.countries.CountryLocal
import de.rki.covpass.sdk.rules.local.rules.CovPassRuleDescriptionLocal
import de.rki.covpass.sdk.rules.local.rules.CovPassRuleLocal
import de.rki.covpass.sdk.rules.local.rules.CovPassRulesDao
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsDao

@Database(
    entities = [
        CovPassRuleLocal::class,
        CovPassRuleDescriptionLocal::class,
        CovPassValueSetLocal::class,
        BoosterDescriptionLocal::class,
        BoosterRuleLocal::class,
        CountryLocal::class
    ],
    version = 4
)

@TypeConverters(Converters::class)
public abstract class CovPassDatabase : RoomDatabase() {

    public abstract fun covPassRulesDao(): CovPassRulesDao

    public abstract fun covPassValueSetsDao(): CovPassValueSetsDao

    public abstract fun boosterRulesDao(): BoosterRulesDao

    public abstract fun countriesDao(): CountriesDao
}
