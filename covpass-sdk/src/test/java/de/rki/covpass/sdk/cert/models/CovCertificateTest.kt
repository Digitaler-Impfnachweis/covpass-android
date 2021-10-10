/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.BaseSdkTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CovCertificateTest : BaseSdkTest() {

    @Test(expected = IllegalStateException::class)
    fun `CovCertificate without DGCEntries shall not be possible`() {
        CovCertificate(vaccinations = null, tests = null, recoveries = null)
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
        assertEquals("$givenName $familyName", certAllNames.fullName)
        assertEquals("$familyName, $givenName", certAllNames.fullNameReverse)

        val certDefaultNames = CovCertificate(
            name = Name(
                givenName = givenName,
                familyName = familyName,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertEquals("$givenName $familyName", certDefaultNames.fullName)
        assertEquals("$familyName, $givenName", certDefaultNames.fullNameReverse)

        val certTransliteratedNames = CovCertificate(
            name = Name(
                givenNameTransliterated = givenNameTransliterated,
                familyNameTransliterated = familyNameTransliterated,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertEquals("$givenNameTransliterated $familyNameTransliterated", certTransliteratedNames.fullName)
        assertEquals("$familyNameTransliterated, $givenNameTransliterated", certTransliteratedNames.fullNameReverse)

        val certFamilyTransliteratedNames = CovCertificate(
            name = Name(
                familyNameTransliterated = familyNameTransliterated,
            ),
            vaccinations = listOf(Vaccination()),
        )
        assertEquals(familyNameTransliterated, certFamilyTransliteratedNames.fullName)
        assertEquals(familyNameTransliterated, certFamilyTransliteratedNames.fullNameReverse)
    }
}
