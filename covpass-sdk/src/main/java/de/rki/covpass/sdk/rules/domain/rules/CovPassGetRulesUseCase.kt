/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.domain.rules

import de.rki.covpass.sdk.rules.CovPassRule
import de.rki.covpass.sdk.rules.CovPassRulesRepository
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public class CovPassGetRulesUseCase(
    private val covPassRulesRepository: CovPassRulesRepository,
) {

    public suspend fun invoke(
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
        region: String? = null,
    ): List<CovPassRule> {
        val filteredAcceptanceRules = mutableMapOf<String, CovPassRule>()
        val selectedRegion: String = region?.trim() ?: ""
        val acceptanceRules = covPassRulesRepository.getRulesBy(
            acceptanceCountryIsoCode,
            validationClock,
            Type.ACCEPTANCE,
            certificateType.toRuleCertificateType(),
        )
        for (rule in acceptanceRules) {
            val ruleRegion: String = rule.region?.trim() ?: ""
            if (selectedRegion.equals(
                    ruleRegion,
                    ignoreCase = true,
                ) && (
                    filteredAcceptanceRules[rule.identifier]?.version?.toVersion()
                        ?: -1 < rule.version.toVersion() ?: 0
                    )
            ) {
                filteredAcceptanceRules[rule.identifier] = rule
            }
        }

        val filteredInvalidationRules = mutableMapOf<String, CovPassRule>()
        if (issuanceCountryIsoCode.isNotBlank()) {
            val invalidationRules = covPassRulesRepository.getRulesBy(
                issuanceCountryIsoCode,
                validationClock,
                Type.INVALIDATION,
                certificateType.toRuleCertificateType(),
            )
            for (rule in invalidationRules) {
                if (
                    filteredInvalidationRules[rule.identifier]?.version?.toVersion()
                    ?: -1 < rule.version.toVersion() ?: 0
                ) {
                    filteredInvalidationRules[rule.identifier] = rule
                }
            }
        }
        return filteredAcceptanceRules.values + filteredInvalidationRules.values
    }

    private fun String.toVersion(): Int? = try {
        val versionParts = this.split('.')
        var version = 0
        var multiplier = 1
        versionParts.reversed().forEach {
            version += multiplier * it.toInt()
            multiplier *= 100
        }
        version
    } catch (error: Throwable) {
        null
    }
}
