/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.eu

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.rki.covpass.sdk.rules.CovPassRule
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.Rule

public fun CovPassRule.toCovPassRuleWithDescriptionLocal(): CovPassEuRuleWithDescriptionsLocal =
    CovPassEuRuleWithDescriptionsLocal(toCovPassRuleLocal(), descriptions.toCovPassDescriptionsLocal())

public fun Collection<CovPassRule>.toCovPassRulesWithDescriptionLocal(): List<CovPassEuRuleWithDescriptionsLocal> =
    map { it.toCovPassRuleWithDescriptionLocal() }

public fun CovPassRule.toCovPassRuleLocal(): CovPassEuRuleLocal = CovPassEuRuleLocal(
    identifier = identifier,
    type = type,
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    ruleCertificateType = ruleCertificateType,
    validFrom = validFrom.withZoneSameInstant(UTC_ZONE_ID),
    validTo = validTo.withZoneSameInstant(UTC_ZONE_ID),
    affectedString = affectedString,
    logic = logic,
    countryCode = countryCode,
    region = region,
    hash = hash,
)

public fun Map<String, String>.toCovPassDescriptionsLocal(): List<CovPassEuRuleDescriptionLocal> =
    map { CovPassEuRuleDescriptionLocal(lang = it.key, desc = it.value) }

public fun Collection<CovPassEuRuleDescriptionLocal>.toDescriptions(): Map<String, String> =
    map { it.lang.lowercase() to it.desc }.toMap()

public fun CovPassEuRuleWithDescriptionsLocal.toCovPassRule(): CovPassRule = CovPassRule(
    identifier = rule.identifier,
    type = rule.type,
    version = rule.version,
    schemaVersion = rule.schemaVersion,
    engine = rule.engine,
    engineVersion = rule.engineVersion,
    ruleCertificateType = rule.ruleCertificateType,
    validFrom = rule.validFrom.withZoneSameInstant(UTC_ZONE_ID),
    validTo = rule.validTo.withZoneSameInstant(UTC_ZONE_ID),
    affectedString = rule.affectedString,
    logic = rule.logic,
    countryCode = rule.countryCode,
    descriptions = descriptions.toDescriptions(),
    region = rule.region,
    hash = rule.hash,
)

public fun Collection<CovPassEuRuleWithDescriptionsLocal>.toCovPassRules(): List<CovPassRule> =
    map { it.toCovPassRule() }

public fun CovPassRule.toRule(): Rule =
    Rule(
        identifier = identifier,
        type = type,
        version = version,
        schemaVersion = schemaVersion,
        engine = engine,
        engineVersion = engineVersion,
        ruleCertificateType = ruleCertificateType,
        validFrom = validFrom,
        validTo = validTo,
        affectedString = affectedString,
        logic = jacksonObjectMapper().readTree(logic),
        countryCode = countryCode,
        descriptions = descriptions,
        region = region,
    )

public fun Collection<CovPassRule>.toRules(): List<Rule> =
    map { it.toRule() }
