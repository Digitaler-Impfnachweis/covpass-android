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
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRuleDescriptionLocal
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRuleLocal
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRulesDao
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRuleDescriptionLocal
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRuleLocal
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRulesDao
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsDao

@Database(
    entities = [
        CovPassEuRuleLocal::class,
        CovPassEuRuleDescriptionLocal::class,
        CovPassDomesticRuleLocal::class,
        CovPassDomesticRuleDescriptionLocal::class,
        CovPassValueSetLocal::class,
        BoosterDescriptionLocal::class,
        BoosterRuleLocal::class,
        CountryLocal::class,
    ],
    version = 8,
)
@TypeConverters(Converters::class)
public abstract class CovPassDatabase : RoomDatabase() {

    public abstract fun covPassEuRulesDao(): CovPassEuRulesDao

    public abstract fun covPassDomesticRulesDao(): CovPassDomesticRulesDao

    public abstract fun covPassValueSetsDao(): CovPassValueSetsDao

    public abstract fun boosterRulesDao(): BoosterRulesDao

    public abstract fun countriesDao(): CountriesDao
}
