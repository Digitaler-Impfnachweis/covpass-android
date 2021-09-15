package dgca.verifier.app.engine

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Rule

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
 * Created by osarapulov on 11.06.21 11:10
 */
class DefaultCertLogicEngine(
    private val affectedFieldsDataRetriever: AffectedFieldsDataRetriever,
    private val jsonLogicValidator: JsonLogicValidator
) : CertLogicEngine {
    private val objectMapper = ObjectMapper()

    companion object {
        private const val EXTERNAL_KEY = "external"
        private const val PAYLOAD_KEY = "payload"
        private const val CERTLOGIC_KEY = "CERTLOGIC"
        private val CERTLOGIC_VERSION: Triple<Int, Int, Int> = Triple(1, 0, 0)
    }

    init {
        objectMapper.findAndRegisterModules()
    }

    private fun prepareData(
        externalParameter: ExternalParameter,
        payload: String
    ): ObjectNode = objectMapper.createObjectNode().apply {
        this.set<JsonNode>(
            EXTERNAL_KEY,
            objectMapper.readValue(objectMapper.writeValueAsString(externalParameter))
        )
        this.set<JsonNode>(
            PAYLOAD_KEY,
            objectMapper.readValue<JsonNode>(payload)
        )
    }

    override fun validate(
        certificateType: CertificateType,
        hcertVersionString: String,
        rules: List<Rule>,
        externalParameter: ExternalParameter,
        payload: String
    ): List<ValidationResult> {
        if (rules.isEmpty()) return emptyList()

        val dataJsonNode = prepareData(externalParameter, payload)
        val hcertVersion: Triple<Int, Int, Int>? = hcertVersionString.toVersion()

        return rules.map {
            checkRule(
                rule = it,
                dataJsonNode = dataJsonNode,
                hcertVersion = hcertVersion,
                certificateType = certificateType
            )
        }
    }

    private fun checkRule(
        rule: Rule,
        dataJsonNode: ObjectNode,
        hcertVersion: Triple<Int, Int, Int>?,
        certificateType: CertificateType
    ): ValidationResult {
        val ruleEngineVersion = rule.engineVersion.toVersion()
        val schemaVersion = rule.schemaVersion.toVersion()

        val validationErrors = mutableListOf<Exception>()

        val isCompatibleVersion = rule.engine == CERTLOGIC_KEY &&
                ruleEngineVersion != null &&
                CERTLOGIC_VERSION.isGreaterOrEqualThan(ruleEngineVersion) &&
                hcertVersion != null &&
                schemaVersion != null &&
                hcertVersion.first == schemaVersion.first &&
                hcertVersion.isGreaterOrEqualThan(schemaVersion)

        val res = if (isCompatibleVersion) {
            try {
                when (jsonLogicValidator.isDataValid(rule.logic, dataJsonNode)) {
                    true -> Result.PASSED
                    false -> Result.FAIL
                }
            } catch (e: Exception) {
                validationErrors.add(e)
                Result.OPEN
            }
        } else {
            Result.OPEN
        }

        val cur: String = affectedFieldsDataRetriever.getAffectedFieldsData(
            rule,
            dataJsonNode,
            certificateType
        )

        return ValidationResult(
            rule,
            res,
            cur,
            if (validationErrors.isEmpty()) null else validationErrors
        )
    }

    private fun Triple<Int, Int, Int>.isGreaterOrEqualThan(version: Triple<Int, Int, Int>): Boolean =
        first > version.first || (first == version.first && (second > version.second || (second == version.second && third >= version.third)))

    /**
     * Tries to convert String into a version based on pattern majorVersion.minorVersion.patchVersion.
     */
    private fun String.toVersion(): Triple<Int, Int, Int>? = try {
        val versionPieces = this.split('.')
        Triple(versionPieces[0].toInt(), versionPieces[1].toInt(), versionPieces[2].toInt())
    } catch (error: Throwable) {
        null
    }
}