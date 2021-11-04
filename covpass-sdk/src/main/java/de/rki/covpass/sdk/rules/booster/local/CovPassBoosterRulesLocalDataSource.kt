/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.local

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.booster.BoosterRule
import kotlinx.coroutines.invoke
import java.time.ZonedDateTime

public class CovPassBoosterRulesLocalDataSource(
    private val boosterRulesDao: BoosterRulesDao,
) {

    public suspend fun replaceRules(keep: Collection<String>, boosterRules: List<BoosterRule>) {
        dispatchers.io {
            boosterRulesDao.replaceAll(keep = keep, add = boosterRules.toBoosterRulesWithDescriptionLocal())
        }
    }

    public suspend fun getAllBoosterRules(): List<BoosterRuleLocal> =
        dispatchers.io {
            boosterRulesDao.getAll()
        }

    public suspend fun getAllBoosterRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<BoosterRule> =
        dispatchers.io {
            boosterRulesDao.getBoosterRulesWithDescriptionsBy(
                countryIsoCode,
                validationClock,
            ).toBoosterRules()
        }

    public suspend fun deleteAll() {
        dispatchers.io {
            boosterRulesDao.deleteAll()
        }
    }
}
