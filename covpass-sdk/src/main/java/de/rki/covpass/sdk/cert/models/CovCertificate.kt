/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.IllegalStateException
import java.time.Instant
import java.time.LocalDate

/**
 * Data model for the CovPass certificates, that can contain [Vaccination], [Test] or [Recovery].
 */
@Serializable
public data class CovCertificate(

    // Information inside the CBOR Web Token (CWT)
    val issuer: String = "",
    @Contextual
    val validFrom: Instant? = null,
    @Contextual
    val validUntil: Instant? = null,

    // The EU Digital Green Certificate
    @SerialName("nam")
    val name: Name = Name(),
    @Contextual
    @SerialName("dob")
    val birthDate: LocalDate? = null,

    // According to latest EU specification the lists should not be nullable.
    // But some countries use null values here, so we have to support it.
    @SerialName("v")
    private val vaccinations: List<Vaccination>? = emptyList(),
    @SerialName("t")
    private val tests: List<Test>? = emptyList(),
    @SerialName("r")
    private val recoveries: List<Recovery>? = emptyList(),

    @SerialName("ver")
    val version: String = "",
) {

    init {
        // Access dgcEntry to throw exception directly if no dgcEntry is contained.
        dgcEntry
    }

    public val dgcEntry: DGCEntry
        get() = vaccinations?.firstOrNull() ?: tests?.firstOrNull() ?: recoveries?.firstOrNull()
            ?: throw IllegalStateException("CovCertificates without any DGCEntries are not allowed.")

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val vaccination: Vaccination?
        get() = vaccinations?.firstOrNull()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val test: Test?
        get() = tests?.firstOrNull()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val recovery: Recovery?
        get() = recoveries?.firstOrNull()

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
}
