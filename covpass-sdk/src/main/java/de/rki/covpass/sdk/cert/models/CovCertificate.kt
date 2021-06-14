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
    private val vaccinations: List<Vaccination> = emptyList(),
    @SerialName("t")
    private val tests: List<Test> = emptyList(),
    @SerialName("r")
    private val recoveries: List<Recovery> = emptyList(),
    @SerialName("ver")
    val version: String = "",
) {
    public val dgcEntry: DGCEntry
        get() = (vaccinations + tests + recoveries).first()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val vaccination: Vaccination?
        get() = vaccinations.firstOrNull()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val test: Test?
        get() = tests.firstOrNull()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val recovery: Recovery?
        get() = recoveries.firstOrNull()

    public val fullName: String by lazy {
        listOfNotNull(
            name.givenName ?: name.givenNameTransliterated,
            name.familyName ?: name.familyNameTransliterated
        ).joinToString(" ")
    }

    public val fullNameReverse: String by lazy {
        listOfNotNull(
            name.familyName ?: name.familyNameTransliterated,
            name.givenName ?: name.givenNameTransliterated
        ).joinToString(", ")
    }

    public val validDate: LocalDate?
        get() = vaccination?.occurrence?.plusDays(15)

    public companion object {
        // The first two numbers of e.g. 1.0.0
        public const val SUPPORTED_MAJOR_VERSION: Int = 1
        public const val SUPPORTED_MINOR_VERSION: Int = 3
    }
}
