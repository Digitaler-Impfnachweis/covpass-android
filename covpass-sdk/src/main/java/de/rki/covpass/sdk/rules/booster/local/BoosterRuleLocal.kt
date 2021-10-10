/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.covpass.sdk.rules.booster.BoosterType
import dgca.verifier.app.engine.data.RuleCertificateType
import java.time.ZonedDateTime

@Entity(tableName = "booster_rules")
public data class BoosterRuleLocal(
    @PrimaryKey(autoGenerate = true)
    val ruleId: Long = 0,
    val identifier: String,
    val type: BoosterType,
    val version: String,
    val schemaVersion: String,
    val engine: String,
    val engineVersion: String,
    val ruleCertificateType: RuleCertificateType,
    val validFrom: ZonedDateTime,
    val validTo: ZonedDateTime,
    val affectedString: List<String>,
    val logic: String,
    val countryCode: String,
    val region: String?,
    val hash: String,
)
