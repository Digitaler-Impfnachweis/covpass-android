/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.local

import de.rki.covpass.sdk.rules.booster.BoosterRule
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.Description

public fun BoosterRule.toBoosterRuleWithDescriptionLocal(): BoosterRuleWithDescriptionsLocal =
    BoosterRuleWithDescriptionsLocal(toBoosterRuleLocal(), descriptions.toBoosterDescriptionsLocal())

public fun Collection<BoosterRule>.toBoosterRulesWithDescriptionLocal(): List<BoosterRuleWithDescriptionsLocal> =
    map { it.toBoosterRuleWithDescriptionLocal() }

public fun BoosterRule.toBoosterRuleLocal(): BoosterRuleLocal = BoosterRuleLocal(
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
    hash = hash
)

public fun Map<String, String>.toBoosterDescriptionsLocal(): List<BoosterDescriptionLocal> =
    map { BoosterDescriptionLocal(lang = it.key, desc = it.value) }

public fun BoosterDescriptionLocal.toBoosterDescription(): Description =
    Description(lang = lang, desc = desc)

public fun Collection<BoosterDescriptionLocal>.toBoosterDescriptions(): Map<String, String> =
    map { it.lang.lowercase() to it.desc }.toMap()

public fun BoosterRuleWithDescriptionsLocal.toBoosterRule(): BoosterRule = BoosterRule(
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
    descriptions = descriptions.toBoosterDescriptions(),
    region = rule.region,
    hash = rule.hash
)

public fun Collection<BoosterRuleWithDescriptionsLocal>.toBoosterRules(): List<BoosterRule> =
    map { it.toBoosterRule() }
