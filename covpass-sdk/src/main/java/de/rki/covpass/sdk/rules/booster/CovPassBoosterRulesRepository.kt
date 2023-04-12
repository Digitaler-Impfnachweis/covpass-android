/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster

import de.rki.covpass.sdk.rules.booster.local.BoosterRuleLocal
import de.rki.covpass.sdk.rules.booster.local.CovPassBoosterRulesLocalDataSource
import java.time.ZonedDateTime

public class CovPassBoosterRulesRepository(
    private val localDataSource: CovPassBoosterRulesLocalDataSource,
) {

    public suspend fun getAllBoosterRules(): List<BoosterRuleLocal> {
        return localDataSource.getAllBoosterRules()
    }

    public suspend fun prepopulate(rules: List<BoosterRule>) {
        localDataSource.replaceRules(keep = emptyList(), boosterRules = rules)
    }

    public suspend fun getCovPassBoosterRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<BoosterRule> =
        localDataSource.getAllBoosterRulesBy(countryIsoCode, validationClock)

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
