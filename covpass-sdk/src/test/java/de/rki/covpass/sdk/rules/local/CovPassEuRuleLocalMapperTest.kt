/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.covpass.sdk.rules.CovPassRule
import de.rki.covpass.sdk.rules.local.rules.eu.*
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

public class CovPassEuRuleLocalMapperTest {

    private val date by lazy {
        ZonedDateTime.now()
    }
    private val covPassRule by lazy {
        CovPassRule(
            identifier = "identifier",
            type = Type.ACCEPTANCE,
            version = "version",
            schemaVersion = "schemaVersion",
            engine = "engine",
            engineVersion = "engineVersion",
            ruleCertificateType = RuleCertificateType.GENERAL,
            validFrom = date.withZoneSameInstant(UTC_ZONE_ID),
            validTo = date.plusDays(2).withZoneSameInstant(UTC_ZONE_ID),
            affectedString = listOf("affectedString"),
            logic = "{\"value\": 1}",
            countryCode = "countryCode",
            region = "region",
            hash = "hash",
            descriptions = mapOf(Pair("description", "value"))
        )
    }
    private val covPassRuleWithDescription by lazy {
        CovPassEuRuleWithDescriptionsLocal(
            covPassRuleLocal,
            descriptions = listOf(CovPassEuRuleDescriptionLocal(lang = "description", desc = "value"))
        )
    }
    private val covPassRuleLocal by lazy {
        CovPassEuRuleLocal(
            identifier = "identifier",
            type = Type.ACCEPTANCE,
            version = "version",
            schemaVersion = "schemaVersion",
            engine = "engine",
            engineVersion = "engineVersion",
            ruleCertificateType = RuleCertificateType.GENERAL,
            validFrom = date.withZoneSameInstant(UTC_ZONE_ID),
            validTo = date.plusDays(2).withZoneSameInstant(UTC_ZONE_ID),
            affectedString = listOf("affectedString"),
            logic = "{\"value\": 1}",
            countryCode = "countryCode",
            region = "region",
            hash = "hash"
        )
    }
    private val rule by lazy {
        Rule(
            identifier = "identifier",
            type = Type.ACCEPTANCE,
            version = "version",
            schemaVersion = "schemaVersion",
            engine = "engine",
            engineVersion = "engineVersion",
            ruleCertificateType = RuleCertificateType.GENERAL,
            validFrom = date.withZoneSameInstant(UTC_ZONE_ID),
            validTo = date.plusDays(2).withZoneSameInstant(UTC_ZONE_ID),
            affectedString = listOf("affectedString"),
            logic = jacksonObjectMapper().readTree("{\"value\": 1}"),
            countryCode = "countryCode",
            descriptions = mapOf(Pair("description", "value")),
            region = "region"
        )
    }

    @Test
    public fun `test toCovPassRuleLocal()`() {
        val convertedCovPassRuleLocal = covPassRule.toCovPassRuleLocal()
        assertEquals(covPassRuleLocal, convertedCovPassRuleLocal)
    }

    @Test
    public fun `test toCovPassDescriptionsLocal() and toDescriptions()`() {
        val map = mapOf(Pair("description", "value"))
        val covPassRuleDescriptionLocalList = listOf(
            CovPassEuRuleDescriptionLocal(lang = "description", desc = "value")
        )

        val convertedCovPassRuleDescriptionLocalList = map.toCovPassDescriptionsLocal()
        assertEquals(convertedCovPassRuleDescriptionLocalList, covPassRuleDescriptionLocalList)

        val convertedDescription = covPassRuleDescriptionLocalList.toDescriptions()
        assertEquals(map, convertedDescription)
    }

    @Test
    public fun `test toCovPassRuleWithDescriptionLocal()`() {
        val covPassRuleWithDescriptionLocal = covPassRule.toCovPassRuleWithDescriptionLocal()
        assertEquals(covPassRuleWithDescriptionLocal, covPassRuleWithDescription)
    }

    @Test
    public fun `test toRule()`() {
        val convertedRule = covPassRule.toRule()
        assertEquals(convertedRule, rule)
    }
}
