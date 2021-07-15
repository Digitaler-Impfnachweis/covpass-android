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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dgca.verifier.app.engine.data.source.local.countries.CountriesDao
import dgca.verifier.app.engine.data.source.local.countries.CountryLocal
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetLocal
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsDao

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
 * Created by osarapulov on 16.06.21 9:05
 */
@Database(
    entities = [RuleLocal::class, DescriptionLocal::class, CountryLocal::class, ValueSetLocal::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class EngineDatabase : RoomDatabase() {
    abstract fun rulesDao(): RulesDao

    abstract fun countriesDao(): CountriesDao

    abstract fun valueSetsDao(): ValueSetsDao
}
