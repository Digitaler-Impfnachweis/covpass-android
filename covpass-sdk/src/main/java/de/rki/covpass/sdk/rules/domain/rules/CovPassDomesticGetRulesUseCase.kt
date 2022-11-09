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

public enum class CovPassValidationType {
    RULES, IMMUNITYSTATUSBTWO, IMMUNITYSTATUSCTWO, IMMUNITYSTATUSETWO, MASK, INVALIDATION
}

public class CovPassDomesticGetRulesUseCase(
    private val covPassRulesRepository: CovPassRulesRepository,
) : CovPassUseCase {

    public override suspend fun invoke(
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
        validationType: CovPassValidationType,
        region: String?,
    ): List<CovPassRule> {
        return when (validationType) {
            CovPassValidationType.RULES -> {
                getAcceptanceAndInvalidationRules(
                    acceptanceCountryIsoCode,
                    issuanceCountryIsoCode,
                    certificateType,
                    validationClock,
                    region,
                )
            }
            CovPassValidationType.INVALIDATION -> {
                getInvalidationRules(
                    issuanceCountryIsoCode,
                    certificateType,
                    validationClock,
                )
            }
            else -> {
                getImmunityStatusAndMaskRules(
                    acceptanceCountryIsoCode,
                    certificateType,
                    validationClock,
                    validationType.toRulesType(),
                    region,
                )
            }
        }
    }

    private suspend fun getAcceptanceAndInvalidationRules(
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
        region: String? = null,
    ): List<CovPassRule> =
        getAcceptanceRules(acceptanceCountryIsoCode, certificateType, validationClock, region) +
            getInvalidationRules(issuanceCountryIsoCode, certificateType, validationClock)

    private suspend fun getAcceptanceRules(
        acceptanceCountryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
        region: String? = null,
    ): List<CovPassRule> {
        val filteredAcceptanceRules = mutableMapOf<String, CovPassRule>()
        val generalRulePredicates = listOf(
            { value: Type -> value == Type.IMPFSTATUSBZWEI },
            { value: Type -> value == Type.IMPFSTATUSCZWEI },
            { value: Type -> value == Type.IMPFSTATUSEZWEI },
            { value: Type -> value == Type.MASK },
        )
        val selectedRegion: String = region?.trim() ?: ""
        val acceptanceRules = covPassRulesRepository.getRulesBy(
            acceptanceCountryIsoCode,
            validationClock,
            Type.ACCEPTANCE,
            certificateType.toRuleCertificateType(),
        ).filterNot { rule -> generalRulePredicates.all { it(rule.type) } }

        for (rule in acceptanceRules) {
            val ruleRegion: String = rule.region?.trim() ?: ""
            if (selectedRegion.equals(
                    ruleRegion,
                    ignoreCase = true,
                ) && (
                    (
                        filteredAcceptanceRules[rule.identifier]?.version?.toVersion()
                            ?: -1
                        ) < (rule.version.toVersion() ?: 0)
                    )
            ) {
                filteredAcceptanceRules[rule.identifier] = rule
            }
        }
        return filteredAcceptanceRules.values.toList()
    }

    private suspend fun getInvalidationRules(
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
    ): List<CovPassRule> {
        val filteredInvalidationRules = mutableMapOf<String, CovPassRule>()
        val generalRulePredicates = listOf(
            { value: Type -> value == Type.IMPFSTATUSBZWEI },
            { value: Type -> value == Type.IMPFSTATUSCZWEI },
            { value: Type -> value == Type.IMPFSTATUSEZWEI },
            { value: Type -> value == Type.MASK },
        )

        if (issuanceCountryIsoCode.isNotBlank()) {
            val invalidationRules = covPassRulesRepository.getRulesBy(
                issuanceCountryIsoCode,
                validationClock,
                Type.INVALIDATION,
                certificateType.toRuleCertificateType(),
            ).filterNot { rule -> generalRulePredicates.all { it(rule.type) } }
            for (rule in invalidationRules) {
                if (
                    (
                        filteredInvalidationRules[rule.identifier]?.version?.toVersion()
                            ?: -1
                        ) < (rule.version.toVersion() ?: 0)
                ) {
                    filteredInvalidationRules[rule.identifier] = rule
                }
            }
        }
        return filteredInvalidationRules.values.toList()
    }

    private fun CovPassValidationType.toRulesType(): Type {
        return when (this) {
            CovPassValidationType.IMMUNITYSTATUSBTWO -> Type.IMPFSTATUSBZWEI
            CovPassValidationType.IMMUNITYSTATUSCTWO -> Type.IMPFSTATUSCZWEI
            CovPassValidationType.IMMUNITYSTATUSETWO -> Type.IMPFSTATUSEZWEI
            CovPassValidationType.MASK -> Type.MASK
            else -> throw IllegalStateException("Validation type already handled: ${this.name}")
        }
    }

    private suspend fun getImmunityStatusAndMaskRules(
        countryIsoCode: String,
        certificateType: CertificateType,
        validationClock: ZonedDateTime,
        type: Type,
        region: String? = null,
    ): List<CovPassRule> {
        val filteredRules = mutableMapOf<String, CovPassRule>()

        val selectedRegion: String = region?.trim() ?: ""
        val rules = covPassRulesRepository.getRulesBy(
            countryIsoCode,
            validationClock,
            type,
            certificateType.toRuleCertificateType(),
        )
        for (rule in rules) {
            val ruleRegion: String = rule.region?.trim() ?: ""
            if (selectedRegion.equals(
                    ruleRegion,
                    ignoreCase = true,
                ) && (
                    (
                        filteredRules[rule.identifier]?.version?.toVersion()
                            ?: -1
                        ) < (rule.version.toVersion() ?: 0)
                    )
            ) {
                filteredRules[rule.identifier] = rule
            }
        }
        return filteredRules.values.toList()
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
