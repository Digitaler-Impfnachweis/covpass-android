package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.rules.CovPassDomesticRulesRepository
import de.rki.covpass.sdk.rules.CovPassRule
import de.rki.covpass.sdk.utils.formatDateDeOrEmpty
import java.time.ZonedDateTime

public class CovPassMaskRulesDateResolver(
    public val covPassDomesticRulesRepository: CovPassDomesticRulesRepository,
) {

    public suspend fun getMaskRuleValidity(
        region: String,
        countryIsoCode: String = "de",
        validationClock: ZonedDateTime = ZonedDateTime.now(),
    ): String {
        val filteredRules = mutableMapOf<String, CovPassRule>()
        val selectedRegion: String = region.trim()

        val maskRules = covPassDomesticRulesRepository.getMaskRules(
            countryIsoCode,
            validationClock,
        )

        for (rule in maskRules) {
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
        return filteredRules.values.sortedByDescending {
            it.validFrom
        }.map {
            it.validFrom
        }.firstOrNull()?.toInstant().formatDateDeOrEmpty()
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
