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
 *  Created by osarapulov on 6/25/21 9:21 AM
 */

package dgca.verifier.app.engine.data.source.remote.rules

import dgca.verifier.app.engine.data.Description
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.util.*

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
 * Created by osarapulov on 17.06.21 15:38
 */
fun RuleRemote.toRule(): Rule = Rule(
    identifier = this.identifier,
    type = Type.valueOf(this.type.toUpperCase(Locale.ROOT)),
    version = this.version,
    schemaVersion = this.schemaVersion,
    engine = this.engine,
    engineVersion = this.engineVersion,
    ruleCertificateType = RuleCertificateType.valueOf(this.certificateType.toUpperCase(Locale.ROOT)),
    descriptions = this.descriptions.toDescriptions(),
    validFrom = this.validFrom,
    validTo = this.validTo,
    affectedString = this.affectedString,
    logic = this.logic,
    countryCode = this.countryCode.toLowerCase(Locale.ROOT),
    region = this.region
)

fun List<RuleRemote>.toRules(): List<Rule> {
    val rules = mutableListOf<Rule>()
    for (i in this.indices) {
        val ruleRemote = this[i]
        rules.add(ruleRemote.toRule())
    }
    return rules
}

fun DescriptionRemote.toDescriptions(): Description = Description(
    lang = this.lang,
    desc = this.desc
)

fun List<DescriptionRemote>.toDescriptions(): Map<String, String> {
    val descriptions = mutableMapOf<String, String>()
    for (i in this.indices) {
        val descriptionRemote = this[i]
        descriptions[descriptionRemote.lang.toLowerCase(Locale.ROOT)] = descriptionRemote.desc
    }
    return descriptions
}