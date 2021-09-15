/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.remote

import de.rki.covpass.sdk.rules.booster.BoosterRule
import de.rki.covpass.sdk.rules.booster.BoosterType
import dgca.verifier.app.engine.data.RuleCertificateType

public fun BoosterRuleRemote.toBoosterRule(hash: String): BoosterRule = BoosterRule(
    identifier = identifier,
    type = BoosterType.valueOf(type.uppercase()),
    version = version,
    schemaVersion = schemaVersion,
    engine = engine,
    engineVersion = engineVersion,
    ruleCertificateType = RuleCertificateType.valueOf(certificateType.uppercase()),
    descriptions = descriptions.toDescriptions(),
    validFrom = validFrom,
    validTo = validTo,
    affectedString = affectedString,
    logic = logic.toString(),
    countryCode = countryCode.lowercase(),
    region = region,
    hash = hash
)

public fun Collection<BoosterDescriptionRemote>.toDescriptions(): Map<String, String> =
    map { it.lang.lowercase() to it.desc }.toMap()
