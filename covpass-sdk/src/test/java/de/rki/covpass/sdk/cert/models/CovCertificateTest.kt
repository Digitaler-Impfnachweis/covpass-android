/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.rki.covpass.sdk.utils.BaseSdkTest
import org.junit.Test
import java.lang.IllegalStateException

internal class CovCertificateTest : BaseSdkTest() {

    @Test(expected = IllegalStateException::class)
    fun `CovCertificate without DGCEntries shall not be possible`() {
        CovCertificate(vaccinations = null, tests = null, recoveries = null)
    }

    @Test(expected = IllegalStateException::class)
    fun `CovCertificate with multiple DGCEntries of different type shall not be possible`() {
        CovCertificate(
            vaccinations = listOf(Vaccination()),
            tests = null,
            recoveries = listOf(Recovery())
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `CovCertificate with multiple DGCEntries of same type shall not be possible`() {
        CovCertificate(
            vaccinations = listOf(Vaccination(), Vaccination()),
            tests = null,
            recoveries = null
        )
    }

    @Test
    fun `Different name combinations shall produce correct display name`() {
        val givenName = "givenName"
        val givenNameTransliterated = "givenNameTransliterated"
        val familyName = "familyName"
        val familyNameTransliterated = "familyNameTransliterated"

        val certAllNames = CovCertificate(
            name = Name(
                givenName = givenName,
                givenNameTransliterated = givenNameTransliterated,
                familyName = familyName,
                familyNameTransliterated = familyNameTransliterated,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertThat(certAllNames.fullName).isEqualTo("$givenName $familyName")
        assertThat(certAllNames.fullNameReverse).isEqualTo("$familyName, $givenName")

        val certDefaultNames = CovCertificate(
            name = Name(
                givenName = givenName,
                familyName = familyName,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertThat(certDefaultNames.fullName).isEqualTo("$givenName $familyName")
        assertThat(certDefaultNames.fullNameReverse).isEqualTo("$familyName, $givenName")

        val certTransliteratedNames = CovCertificate(
            name = Name(
                givenNameTransliterated = givenNameTransliterated,
                familyNameTransliterated = familyNameTransliterated,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertThat(certTransliteratedNames.fullName)
            .isEqualTo("$givenNameTransliterated $familyNameTransliterated")
        assertThat(certTransliteratedNames.fullNameReverse)
            .isEqualTo("$familyNameTransliterated, $givenNameTransliterated")

        val certFamilyTransliteratedNames = CovCertificate(
            name = Name(
                familyNameTransliterated = familyNameTransliterated,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertThat(certFamilyTransliteratedNames.fullName)
            .isEqualTo(familyNameTransliterated)
        assertThat(certFamilyTransliteratedNames.fullNameReverse)
            .isEqualTo(familyNameTransliterated)
    }
}
