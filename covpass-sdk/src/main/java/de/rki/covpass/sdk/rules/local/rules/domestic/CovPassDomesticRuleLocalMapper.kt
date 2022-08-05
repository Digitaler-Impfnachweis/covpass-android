/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.domestic

import de.rki.covpass.sdk.rules.CovPassRule
import dgca.verifier.app.engine.UTC_ZONE_ID

public fun CovPassRule.toCovPassDomesticRuleWithDescriptionLocal(): CovPassDomesticRuleWithDescriptionsLocal =
    CovPassDomesticRuleWithDescriptionsLocal(
        toCovPassDomesticRuleLocal(),
        descriptions.toCovPassDomesticDescriptionsLocal(),
    )

public fun Collection<CovPassRule>.toCovPassDomesticRulesWithDescriptionLocal():
    List<CovPassDomesticRuleWithDescriptionsLocal> =
    map { it.toCovPassDomesticRuleWithDescriptionLocal() }

public fun CovPassRule.toCovPassDomesticRuleLocal(): CovPassDomesticRuleLocal =
    CovPassDomesticRuleLocal(
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

public fun Map<String, String>.toCovPassDomesticDescriptionsLocal():
    List<CovPassDomesticRuleDescriptionLocal> =
    map { CovPassDomesticRuleDescriptionLocal(lang = it.key, desc = it.value) }

public fun Collection<CovPassDomesticRuleDescriptionLocal>.toDescriptions(): Map<String, String> =
    map { it.lang.lowercase() to it.desc }.toMap()

public fun CovPassDomesticRuleWithDescriptionsLocal.toCovPassRule(): CovPassRule = CovPassRule(
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

public fun Collection<CovPassDomesticRuleWithDescriptionsLocal>.toCovPassRules(): List<CovPassRule> =
    map { it.toCovPassRule() }
