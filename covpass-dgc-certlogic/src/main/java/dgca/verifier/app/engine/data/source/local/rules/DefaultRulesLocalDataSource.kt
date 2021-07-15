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
 *  Created by osarapulov on 6/25/21 9:18 AM
 */

package dgca.verifier.app.engine.data.source.local.rules

import dgca.verifier.app.engine.data.Rule
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
 * Created by osarapulov on 13.06.21 16:56
 */
class DefaultRulesLocalDataSource(private val rulesDao: RulesDao) : RulesLocalDataSource {

    override fun addRules(rules: List<Rule>) {
        rulesDao.insertAll(*rules.toRulesWithDescriptionLocal().toTypedArray())
    }

    override fun removeRules() {
        rulesDao.deleteAll()
    }

    override fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule> = rulesDao.getRulesWithDescriptionsBy(
        countryIsoCode,
        validationClock,
        type,
        ruleCertificateType,
        RuleCertificateType.GENERAL
    ).toRules()
}
