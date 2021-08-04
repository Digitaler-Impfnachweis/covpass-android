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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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
 * Created by osarapulov on 16.06.21 9:00
 */
@Dao
abstract class RulesDao {
    @Query("SELECT * from rules")
    abstract fun getAll(): List<RuleLocal>

    @Query("SELECT * from descriptions")
    abstract fun getDescriptionAll(): List<DescriptionLocal>

    @Transaction
    @Query("SELECT * FROM rules WHERE :countryIsoCode = countryCode AND (:validationClock BETWEEN validFrom AND validTo) AND :type = type AND (:ruleCertificateType = ruleCertificateType OR :generalRuleCertificateType = ruleCertificateType)")
    abstract fun getRulesWithDescriptionsBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
        generalRuleCertificateType: RuleCertificateType
    ): List<RuleWithDescriptionsLocal>

    @Transaction
    @Query("SELECT * FROM rules WHERE :countryIsoCode = countryCode")
    abstract fun getRulesWithDescriptionsBy(
        countryIsoCode: String
    ): List<RuleWithDescriptionsLocal>

    @Insert
    abstract fun insertRule(rule: RuleLocal): Long

    @Query("DELETE FROM rules WHERE identifier IN (:identifiers)")
    abstract fun deleteRulesBy(vararg identifiers: String)

    @Insert
    abstract fun insertDescriptions(vararg descriptions: DescriptionLocal)

    fun insertAll(vararg rulesWithDescription: RuleWithDescriptionsLocal) {
        rulesWithDescription.forEach { ruleWithDescriptionsLocal ->
            val rule = ruleWithDescriptionsLocal.rule
            val descriptions = ruleWithDescriptionsLocal.descriptions
            val ruleId = insertRule(rule)
            val descriptionsToBeInserted = mutableListOf<DescriptionLocal>()
            descriptions.forEach { descriptionLocal ->
                descriptionsToBeInserted.add(
                    descriptionLocal.copy(
                        ruleContainerId = ruleId
                    )
                )
            }
            insertDescriptions(*descriptionsToBeInserted.toTypedArray())
        }
    }

    @Query("DELETE FROM rules WHERE identifier NOT IN (:identifiers)")
    abstract fun deleteAllExcept(identifiers: Array<String>)

    @Query("DELETE FROM rules")
    abstract fun deleteAll()
}
