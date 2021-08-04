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

import retrofit2.Response

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
 * Created by osarapulov on 13.06.21 16:54
 */
class DefaultRulesRemoteDataSource(private val rulesApiService: RulesApiService) :
    RulesRemoteDataSource {

    override suspend fun getRuleIdentifiers(rulesUrl: String): List<RuleIdentifierRemote> {
        val rulesResponse: Response<List<RuleIdentifierRemote>> =
            rulesApiService.getRuleIdentifiers(rulesUrl)
        return rulesResponse.body() ?: listOf()
    }

    override suspend fun getRules(rulesUrl: String): List<RuleRemote> {
        val rulesResponse: Response<List<RuleRemote>> = rulesApiService.getRules(rulesUrl)
        return rulesResponse.body() ?: listOf()
    }

    override suspend fun getRule(ruleUrl: String): RuleRemote? {
        val ruleResponse: Response<RuleRemote> = rulesApiService.getRule(ruleUrl)
        return ruleResponse.body()
    }
}
