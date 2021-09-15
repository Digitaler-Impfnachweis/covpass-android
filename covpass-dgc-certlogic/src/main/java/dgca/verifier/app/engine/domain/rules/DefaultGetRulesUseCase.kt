/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by osarapulov on 6/30/21 2:54 PM
 */

package dgca.verifier.app.engine.domain.rules

import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.data.source.rules.RulesRepository
import java.time.ZonedDateTime

/*-
 * ---license-start
 * eu-digital-green-certificates / dgc-certlogic-android
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 *
 * Created by osarapulov on 30.06.21 14:54
 */
class DefaultGetRulesUseCase(private val rulesRepository: RulesRepository) : GetRulesUseCase {
    override fun invoke(
        validationClock: ZonedDateTime,
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        region: String?
    ): List<Rule> {
        val filteredAcceptanceRules = mutableMapOf<String, Rule>()
        val selectedRegion: String = region?.trim() ?: ""
        val acceptanceRules = rulesRepository.getRulesBy(
            acceptanceCountryIsoCode,
            validationClock,
            Type.ACCEPTANCE,
            certificateType.toRuleCertificateType()
        )
        for (i in acceptanceRules.indices) {
            val rule = acceptanceRules[i]
            val ruleRegion: String = rule.region?.trim() ?: ""
            if (selectedRegion.equals(
                    ruleRegion,
                    ignoreCase = true
                ) && (filteredAcceptanceRules[rule.identifier]?.version?.toVersion() ?: -1 < rule.version.toVersion() ?: 0)
            ) {
                filteredAcceptanceRules[rule.identifier] = rule
            }
        }

        val filteredInvalidationRules = mutableMapOf<String, Rule>()
        if (issuanceCountryIsoCode.isNotBlank()) {
            val invalidationRules = rulesRepository.getRulesBy(
                issuanceCountryIsoCode,
                validationClock,
                Type.INVALIDATION,
                certificateType.toRuleCertificateType()
            )
            for (i in invalidationRules.indices) {
                val rule = invalidationRules[i]
                if (filteredInvalidationRules[rule.identifier]?.version?.toVersion() ?: -1 < rule.version.toVersion() ?: 0) {
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