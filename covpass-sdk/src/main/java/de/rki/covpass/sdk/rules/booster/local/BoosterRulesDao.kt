/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import java.time.ZonedDateTime

@Suppress("SpreadOperator")
@Dao
public abstract class BoosterRulesDao {

    @Query("SELECT * from booster_rules")
    public abstract suspend fun getAll(): List<BoosterRuleLocal>

    @Insert
    public abstract suspend fun insertBoosterRule(rule: BoosterRuleLocal): Long

    @Insert
    public abstract suspend fun insertBoosterDescriptions(vararg descriptions: BoosterDescriptionLocal)

    @Transaction
    public open suspend fun insertAll(vararg boosterRulesWithDescription: BoosterRuleWithDescriptionsLocal) {
        boosterRulesWithDescription.forEach { ruleWithDescriptionsLocal ->
            val rule = ruleWithDescriptionsLocal.rule
            val descriptions = ruleWithDescriptionsLocal.descriptions
            val ruleId = insertBoosterRule(rule)
            val descriptionsToBeInserted = mutableListOf<BoosterDescriptionLocal>()
            descriptions.forEach { descriptionLocal ->
                descriptionsToBeInserted.add(
                    descriptionLocal.copy(
                        ruleContainerId = ruleId
                    )
                )
            }
            insertBoosterDescriptions(*descriptionsToBeInserted.toTypedArray())
        }
    }

    @Transaction
    public open suspend fun replaceAll(keep: Collection<String>, add: Collection<BoosterRuleWithDescriptionsLocal>) {
        deleteAll(keep = keep)
        insertAll(*add.toTypedArray())
    }

    @Query("DELETE FROM booster_rules WHERE identifier NOT IN (:keep)")
    public abstract suspend fun deleteAll(keep: Collection<String> = emptyList())

    /* ktlint-disable max-line-length */
    @Suppress("MaxLineLength")
    @Transaction
    @Query("SELECT * FROM booster_rules WHERE :countryIsoCode = countryCode AND (:validationClock BETWEEN validFrom AND validTo)")
    public abstract fun getBoosterRulesWithDescriptionsBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<BoosterRuleWithDescriptionsLocal>
    /* ktlint-enable max-line-length */
}
