/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.rules.eu

import de.rki.covpass.sdk.rules.CovPassRule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type

public fun CovPassRuleRemote.toCovPassRule(hash: String): CovPassRule = CovPassRule(
    identifier = identifier,
    type = Type.valueOf(type.uppercase()),
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
    hash = hash,
)

public fun Collection<CovPassDescriptionRemote>.toDescriptions(): Map<String, String> =
    map { it.lang.lowercase() to it.desc }.toMap()
