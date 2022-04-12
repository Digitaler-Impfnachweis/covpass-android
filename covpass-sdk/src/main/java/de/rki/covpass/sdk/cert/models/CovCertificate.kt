/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeParseException

/**
 * Data model for the CovPass certificates, that can contain [Vaccination], [TestCert] or [Recovery].
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
    @SerialName("dob")
    val birthDate: String = "",

    // According to latest EU specification the lists should not be nullable.
    // But some countries use null values here, so we have to support it.
    @SerialName("v")
    private val vaccinations: List<Vaccination>? = emptyList(),
    @SerialName("t")
    private val tests: List<TestCert>? = emptyList(),
    @SerialName("r")
    private val recoveries: List<Recovery>? = emptyList(),

    @SerialName("ver")
    val version: String = "",

    val kid: String = "",
    val rValue: String = ""
) {

    init {
        // Access dgcEntry to throw exception directly if no dgcEntry is contained.
        dgcEntry
    }

    private val dateTimeSeparator = "T"
    private val empty = 0 // ""
    private val yearCount = 4 // "2021"
    private val yearMonthCount = 7 // "2021-01"
    private val yearMonthDayCount = 10 // "2021-01-03"

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
    public val test: TestCert?
        get() = tests?.firstOrNull()

    /**
     * The EU datamodel representation as a list is an outdated leftover, just publish a single value instead.
     */
    public val recovery: Recovery?
        get() = recoveries?.firstOrNull()

    public val fullName: String by lazy {
        listOfNotNull(
            name.trimmedName.givenName ?: name.trimmedName.givenNameTransliterated,
            name.trimmedName.familyName ?: name.trimmedName.familyNameTransliterated
        ).joinToString(" ")
    }

    public val fullNameReverse: String by lazy {
        listOfNotNull(
            name.trimmedName.familyName ?: name.trimmedName.familyNameTransliterated,
            name.trimmedName.givenName ?: name.trimmedName.givenNameTransliterated
        ).joinToString(", ")
    }

    public val fullTransliteratedName: String by lazy {
        if (!name.trimmedName.givenNameTransliterated.isNullOrEmpty()) {
            listOfNotNull(
                name.trimmedName.givenNameTransliterated,
                name.trimmedName.familyNameTransliterated
            ).joinToString(" ")
        } else {
            name.trimmedName.familyNameTransliterated
        }
    }

    public val fullTransliteratedNameReverse: String by lazy {
        if (!name.trimmedName.givenNameTransliterated.isNullOrEmpty()) {
            listOfNotNull(
                name.trimmedName.familyNameTransliterated,
                name.trimmedName.givenNameTransliterated
            ).joinToString(", ")
        } else {
            name.trimmedName.familyNameTransliterated
        }
    }

    public val validDate: LocalDate?
        get() = vaccination?.validDate

    public val birthDateFormatted: String
        get() = try {
            when (birthDate.count()) {
                empty -> "XXXX-XX-XX"
                yearCount -> "${Year.parse(birthDate)}-XX-XX"
                yearMonthCount -> "${YearMonth.parse(birthDate)}-XX"
                yearMonthDayCount -> "${LocalDate.parse(birthDate)}"
                else -> birthDate.substringBefore(dateTimeSeparator)
            }
        } catch (e: DateTimeParseException) {
            birthDate
        }

    public val isGermanCertificate: Boolean = issuer.equals("DE", ignoreCase = true)
}
