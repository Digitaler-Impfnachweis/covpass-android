/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.accesstoken

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
public data class TicketingCertificateDataRemote(
    val hash: String? = null,
    val lang: String,
    @SerialName("fnt")
    val standardizedFamilyName: String,
    @SerialName("gnt")
    val standardizedGivenName: String,
    @SerialName("dob")
    val dateOfBirth: String,
    val coa: String,
    val cod: String,
    val roa: String,
    val rod: String,
    @SerialName("type")
    val greenCertificateTypes: List<String>,
    val category: List<String>,
    @Contextual
    val validationClock: ZonedDateTime,
    @Contextual
    val validFrom: ZonedDateTime,
    @Contextual
    val validTo: ZonedDateTime,
) {
    val standardizedFamilyNameTrimmed: String by lazy {
        standardizedFamilyName.trim()
    }

    val standardizedGivenNameTrimmed: String by lazy {
        standardizedGivenName.trim()
    }

    val dateOfBirthTrimmed: String by lazy {
        dateOfBirth.trim()
    }

    val greenCertificateTypesTrimmed: List<String> by lazy {
        greenCertificateTypes.map { it.trim() }
    }
}
