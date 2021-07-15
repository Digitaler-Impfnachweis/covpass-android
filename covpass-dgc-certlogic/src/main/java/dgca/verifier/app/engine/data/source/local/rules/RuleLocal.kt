package dgca.verifier.app.engine.data.source.local.rules

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.JsonNode
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
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
 * Created by osarapulov on 16.06.21 8:26
 */
@Entity(tableName = "rules")
data class RuleLocal(
    @PrimaryKey(autoGenerate = true)
    val ruleId: Long = 0,
    val identifier: String,
    val type: Type,
    val version: String,
    val schemaVersion: String,
    val engine: String,
    val engineVersion: String,
    val ruleCertificateType: RuleCertificateType,
    val validFrom: ZonedDateTime,
    val validTo: ZonedDateTime,
    val affectedString: List<String>,
    val logic: JsonNode,
    val countryCode: String,
    val region: String?,
)
