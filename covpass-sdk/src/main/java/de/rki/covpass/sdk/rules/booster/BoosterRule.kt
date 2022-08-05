/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster

import dgca.verifier.app.engine.data.RuleCertificateType
import java.time.ZonedDateTime
import java.util.Locale

public enum class BoosterType {
    BOOSTERNOTIFICATION
}

public data class BoosterRule(
    val identifier: String,
    val type: BoosterType,
    val version: String,
    val schemaVersion: String,
    val engine: String,
    val engineVersion: String,
    val ruleCertificateType: RuleCertificateType,
    val descriptions: Map<String, String>,
    val validFrom: ZonedDateTime,
    val validTo: ZonedDateTime,
    val affectedString: List<String>,
    val logic: String,
    val countryCode: String,
    val region: String?,
    val hash: String,
) {
    public fun getDescriptionFor(languageCode: String): String {
        val description = descriptions[languageCode.lowercase()]
        return if (description?.isNotBlank() == true) description else descriptions[Locale.ENGLISH.language]
            ?: ""
    }
}
