/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.domestic

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassRuleLocal
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

@Entity(tableName = "covpass_domestic_rules")
public data class CovPassDomesticRuleLocal(
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
    val logic: String,
    val countryCode: String,
    val region: String?,
    val hash: String,
) : CovPassRuleLocal
