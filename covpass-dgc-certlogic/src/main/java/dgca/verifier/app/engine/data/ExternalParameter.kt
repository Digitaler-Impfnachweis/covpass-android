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
 *  Created by osarapulov on 6/14/21 1:00 PM
 */

package dgca.verifier.app.engine.data

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
 * Created by osarapulov on 11.06.21 11:08
 */
class ExternalParameter private constructor(
    val validationClock: String,
    val valueSets: Map<String, List<String>>,
    val countryCode: String,
    val exp: String,
    val iat: String,
    val issuerCountryCode: String,
    val kid: String,
    val region: String = ""
) {
    constructor(
        validationClock: ZonedDateTime,
        valueSets: Map<String, List<String>>,
        countryCode: String,
        exp: ZonedDateTime,
        iat: ZonedDateTime,
        issuerCountryCode: String,
        kid: String,
        region: String = ""
    ) : this(
        validationClock = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(validationClock),
        valueSets = valueSets,
        countryCode = countryCode,
        exp = exp.toString(),
        iat = iat.toString(),
        issuerCountryCode = issuerCountryCode,
        kid = kid,
        region = region
    )
}
