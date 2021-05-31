/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

@file:UseSerializers(LocalDateSerializer::class, InstantSerializer::class)

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.serialization.InstantSerializer
import de.rki.covpass.sdk.utils.serialization.LocalDateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant
import java.time.LocalDate

/**
 * Data model for the CovPass certificates, that can contain [Vaccination], [Test] or [Recovery].
 */
@Serializable
public data class CovCertificate(

    // Information inside the CBOR Web Token (CWT)
    val issuer: String = "",
    val validFrom: Instant? = null,
    val validUntil: Instant? = null,

    // The EU Digital Green Certificate
    @SerialName("nam")
    val name: Name = Name(),
    @SerialName("dob")
    val birthDate: LocalDate? = null,
    @SerialName("v")
    val vaccinations: List<Vaccination> = emptyList(),
    @SerialName("t")
    val tests: List<Test> = emptyList(),
    @SerialName("r")
    val recoveries: List<Recovery> = emptyList(),
    @SerialName("ver")
    val version: String = "",
) {
    public val dgcEntry: DGCEntry
        get() = (vaccinations + tests + recoveries).first()

    public val vaccination: Vaccination?
        get() = vaccinations.firstOrNull()

    public val isComplete: Boolean
        get() = vaccinations.any { it.isComplete }

    public val hasFullProtection: Boolean
        get() = vaccinations.any { it.hasFullProtection }

    public val fullName: String by lazy {
        listOfNotNull(
            name.givenName ?: name.givenNameTransliterated,
            name.familyName ?: name.familyNameTransliterated
        ).joinToString(" ")
    }

    public val validDate: LocalDate?
        get() = vaccination?.occurrence?.plusDays(15)
}
