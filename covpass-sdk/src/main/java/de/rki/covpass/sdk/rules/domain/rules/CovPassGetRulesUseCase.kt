/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.domain.rules

import de.rki.covpass.sdk.rules.DefaultCovPassRulesRepository
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.domain.rules.DefaultGetRulesUseCase
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import java.time.ZonedDateTime

public class CovPassGetRulesUseCase(
    private val defaultCovPassRulesRepository: DefaultCovPassRulesRepository
) : CovPassRulesUseCase, GetRulesUseCase by DefaultGetRulesUseCase(defaultCovPassRulesRepository) {

    override suspend fun covPassInvoke(
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        region: String?
    ): List<Rule> {
        val acceptanceRules = mutableMapOf<String, Rule>()
        val selectedRegion: String = region?.trim() ?: ""
        defaultCovPassRulesRepository.getCovPassRulesBy(
            acceptanceCountryIsoCode, ZonedDateTime.now().withZoneSameInstant(
            UTC_ZONE_ID
        ), Type.ACCEPTANCE, certificateType.toRuleCertificateType()
        ).forEach {
            val ruleRegion: String = it.region?.trim() ?: ""
            if (selectedRegion.equals(
                    ruleRegion,
                    ignoreCase = true
                ) && (acceptanceRules[it.identifier]?.version?.toVersion() ?: -1 < it.version.toVersion() ?: 0)
            ) {
                acceptanceRules[it.identifier] = it
            }
        }

        val invalidationRules = mutableMapOf<String, Rule>()
        if (issuanceCountryIsoCode.isNotBlank()) {
            defaultCovPassRulesRepository.getCovPassRulesBy(
                issuanceCountryIsoCode, ZonedDateTime.now().withZoneSameInstant(
                UTC_ZONE_ID
            ), Type.INVALIDATION, certificateType.toRuleCertificateType()
            ).forEach {
                if (invalidationRules[it.identifier]?.version?.toVersion() ?: -1 < it.version.toVersion() ?: 0) {
                    invalidationRules[it.identifier] = it
                }
            }
        }
        return acceptanceRules.values + invalidationRules.values
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
