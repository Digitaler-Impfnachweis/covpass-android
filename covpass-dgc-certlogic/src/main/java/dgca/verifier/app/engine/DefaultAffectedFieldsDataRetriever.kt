package dgca.verifier.app.engine

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import eu.ehn.dcc.certlogic.evaluate

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
 * Created by osarapulov on 01.07.21 13:49
 */
class DefaultAffectedFieldsDataRetriever(
    private val schemaJsonNode: JsonNode,
    private val objectMapper: ObjectMapper
) :
    AffectedFieldsDataRetriever {
    override fun getAffectedFieldsData(
        rule: Rule,
        dataJsonNode: JsonNode,
        certificateType: CertificateType
    ): String {
        var affectedFields = StringBuilder()

        for (i in rule.affectedString.indices) {
            val affectedFiledString = rule.affectedString[i]
            val description: String? = try {
                val res = evaluate(
                    objectMapper.readTree(
                        "{\"var\": \"${
                            certificateType.getSchemaPath(
                                affectedFiledString.split('.').last()
                            )
                        }\"}"
                    ),
                    schemaJsonNode
                )
                if (res is NullNode) null else res.toPrettyString()
            } catch (error: Throwable) {
                null
            }
            val value: String? = try {
                evaluate(
                    objectMapper.readTree("{\"var\": \"payload.$affectedFiledString\"}"),
                    dataJsonNode
                ).toPrettyString()
            } catch (error: Throwable) {
                null
            }
            if (description?.isNotBlank() == true && value?.isNotBlank() == true) {
                affectedFields = affectedFields.append(
                    "$description: $value\n"
                )
            }
        }

        return affectedFields.toString()
    }
}

private fun CertificateType.getSchemaPath(key: String): String {
    val subPath = when (this) {
        CertificateType.TEST -> "test_entry"
        CertificateType.RECOVERY -> "recovery_entry"
        CertificateType.VACCINATION -> "vaccination_entry"
    }
    return "\$defs.$subPath.properties.$key.description"
}