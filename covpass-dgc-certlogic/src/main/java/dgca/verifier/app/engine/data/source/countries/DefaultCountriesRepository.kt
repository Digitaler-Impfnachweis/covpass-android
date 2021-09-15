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
 *  Created by osarapulov on 6/25/21 9:28 AM
 */

package dgca.verifier.app.engine.data.source.countries

import dgca.verifier.app.engine.data.source.local.countries.CountriesLocalDataSource
import dgca.verifier.app.engine.data.source.remote.countries.CountriesRemoteDataSrouce
import kotlinx.coroutines.flow.Flow
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
 * Created by osarapulov on 25.06.21 9:28
 */
class DefaultCountriesRepository(
    private val remoteDataSource: CountriesRemoteDataSrouce,
    private val localDataSource: CountriesLocalDataSource
) : CountriesRepository {

    override suspend fun preLoadCountries(countriesUrl: String) {
        remoteDataSource.getCountries(countriesUrl)
            .map { it.toLowerCase(Locale.ROOT) }
            .apply { localDataSource.updateCountries(this) }
    }

    override fun getCountries(): Flow<List<String>> {
        return localDataSource.getCountries()
    }
}
