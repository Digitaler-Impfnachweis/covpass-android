/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.domestic

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

@Suppress("SpreadOperator")
@Dao
public abstract class CovPassDomesticRulesDao {

    @Query("SELECT * from covpass_domestic_rules")
    public abstract suspend fun getAll(): List<CovPassDomesticRuleLocal>

    @Insert
    public abstract suspend fun insertRule(rule: CovPassDomesticRuleLocal): Long

    @Insert
    public abstract suspend fun insertDescriptions(vararg descriptions: CovPassDomesticRuleDescriptionLocal)

    @Transaction
    public open suspend fun insertAll(vararg covPassRulesWithDescription: CovPassDomesticRuleWithDescriptionsLocal) {
        covPassRulesWithDescription.forEach { ruleWithDescriptionsLocal ->
            val rule = ruleWithDescriptionsLocal.rule
            val descriptions = ruleWithDescriptionsLocal.descriptions
            val ruleId = insertRule(rule)
            val descriptionsToBeInserted = mutableListOf<CovPassDomesticRuleDescriptionLocal>()
            descriptions.forEach { descriptionLocal ->
                descriptionsToBeInserted.add(
                    descriptionLocal.copy(
                        ruleContainerId = ruleId
                    )
                )
            }
            insertDescriptions(*descriptionsToBeInserted.toTypedArray())
        }
    }

    @Transaction
    public open suspend fun replaceAll(
        keep: Collection<String>,
        add: Collection<CovPassDomesticRuleWithDescriptionsLocal>
    ) {
        deleteAll(keep = keep)
        insertAll(*add.toTypedArray())
    }

    @Query("DELETE FROM covpass_domestic_rules WHERE identifier NOT IN (:keep)")
    public abstract suspend fun deleteAll(keep: Collection<String> = emptyList())

    /* ktlint-disable max-line-length */
    @Suppress("MaxLineLength")
    @Transaction
    @Query("SELECT * FROM covpass_domestic_rules WHERE :countryIsoCode = countryCode AND (:validationClock BETWEEN validFrom AND validTo)")
    public abstract fun getRulesWithDescriptionsBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<CovPassDomesticRuleWithDescriptionsLocal>
    /* ktlint-enable max-line-length */

    /* ktlint-disable max-line-length */
    @Suppress("MaxLineLength")
    @Transaction
    @Query("SELECT * FROM covpass_domestic_rules WHERE :countryIsoCode = countryCode AND (:validationClock BETWEEN validFrom AND validTo) AND :type = type AND (:ruleCertificateType = ruleCertificateType OR :generalRuleCertificateType = ruleCertificateType)")
    public abstract fun getRulesWithDescriptionsBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
        generalRuleCertificateType: RuleCertificateType
    ): List<CovPassDomesticRuleWithDescriptionsLocal>
    /* ktlint-enable max-line-length */

    @Transaction
    @Query("SELECT * FROM covpass_domestic_rules WHERE :countryIsoCode = countryCode")
    public abstract fun getRulesWithDescriptionsBy(
        countryIsoCode: String
    ): List<CovPassDomesticRuleWithDescriptionsLocal>
}
