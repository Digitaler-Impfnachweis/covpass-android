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
 *  Created by osarapulov on 6/25/21 3:49 PM
 */

package dgca.verifier.app.engine.data.source.valuesets

import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsLocalDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.toValueSets

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
 * Created by osarapulov on 25.06.21 15:49
 */
class DefaultValueSetsRepository(
    private val remoteDataSource: ValueSetsRemoteDataSource,
    private val localDataSource: ValueSetsLocalDataSource
) : ValueSetsRepository {
    override suspend fun preLoad(url: String) {
        val valueSetsRemote = mutableListOf<ValueSetRemote>()
        val valueSetsIdentifiersRemote = remoteDataSource.getValueSetsIdentifiers(url)

        for (i in valueSetsIdentifiersRemote.indices) {
            val ruleIdentifierRemote = valueSetsIdentifiersRemote[i]
            val valueSetRemote =
                remoteDataSource.getValueSet("$url/${ruleIdentifierRemote.hash}")
            if (valueSetRemote != null) {
                valueSetsRemote.add(valueSetRemote)
            }
        }

        if (valueSetsRemote.isNotEmpty()) {
            localDataSource.updateValueSets(valueSetsRemote.toValueSets())
        }
    }

    override suspend fun getValueSets(): List<ValueSet> = localDataSource.getValueSets()
}