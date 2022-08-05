/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.covpass.sdk.rules.booster.BoosterRule
import dgca.verifier.app.engine.JsonLogicValidator
import dgca.verifier.app.engine.data.ExternalParameter

public class BoosterCertLogicEngine(
    private val jsonLogicValidator: JsonLogicValidator,
) {
    private val objectMapper = ObjectMapper()

    public companion object {
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
        payload: String,
    ): ObjectNode = objectMapper.createObjectNode().apply {
        this.set<JsonNode>(
            EXTERNAL_KEY,
            objectMapper.readValue(objectMapper.writeValueAsString(externalParameter)),
        )
        this.set<JsonNode>(
            PAYLOAD_KEY,
            objectMapper.readValue<JsonNode>(payload),
        )
    }

    public fun validate(
        hcertVersionString: String,
        rules: List<BoosterRule>,
        externalParameter: ExternalParameter,
        payload: String,
    ): List<BoosterValidationResult> {
        if (rules.isEmpty()) return emptyList()

        val dataJsonNode = prepareData(externalParameter, payload)
        val hcertVersion: Triple<Int, Int, Int>? = hcertVersionString.toVersion()

        return rules.map {
            checkRule(
                rule = it,
                dataJsonNode = dataJsonNode,
                hcertVersion = hcertVersion,
            )
        }
    }

    private fun checkRule(
        rule: BoosterRule,
        dataJsonNode: ObjectNode,
        hcertVersion: Triple<Int, Int, Int>?,
    ): BoosterValidationResult {
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
                when (jsonLogicValidator.isDataValid(jacksonObjectMapper().readTree(rule.logic), dataJsonNode)) {
                    true -> BoosterResult.PASSED
                    false -> BoosterResult.FAIL
                }
            } catch (e: Exception) {
                validationErrors.add(e)
                BoosterResult.OPEN
            }
        } else {
            BoosterResult.OPEN
        }

        return BoosterValidationResult(
            rule,
            res,
            if (validationErrors.isEmpty()) null else validationErrors,
        )
    }

    private fun Triple<Int, Int, Int>.isGreaterOrEqualThan(version: Triple<Int, Int, Int>): Boolean =
        first > version.first ||
            (
                first == version.first &&
                    (
                        second > version.second || (second == version.second && third >= version.third)
                        )
                )

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
