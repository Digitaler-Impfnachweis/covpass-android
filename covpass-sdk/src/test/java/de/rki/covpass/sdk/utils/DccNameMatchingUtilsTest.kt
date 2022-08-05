/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Name
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.DccNameMatchingUtils.isHolderSame
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DccNameMatchingUtilsTest {

    @Test
    fun `test holder match`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match with different birthDate`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1989-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match with different givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ANGELIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match with different familyName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "BEISPIELFRAU"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match with optional middle name`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA<MARIA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match with last name addendum`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN<GABLER"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for twins`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ANGELIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for twins with same middle name`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA<MARIA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ANGELIKA<MARIA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for siblings with same middle name (different birthdate)`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA<MARIA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ANGELIKA<MARIA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1989-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for trailing chevron`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "<ERIKA<", familyNameTransliterated = "<MUSTERMANN<"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for leading trailing chevron different givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "<ANGELIKA<", familyNameTransliterated = "<MUSTERMANN<"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for leading trailing whitespace`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = " ERIKA ", familyNameTransliterated = " MUSTERMANN "),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for leading trailing whitespace different givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = " ANGELIKA ", familyNameTransliterated = " MUSTERMANN "),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (no whitespace) in familyName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "DR<MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (one whitespace) in familyName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "DR<<MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (no whitespace) in givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "DR<ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (one whitespace) in givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "DR<<ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertTrue(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (one whitespace) in different givenName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "DR<<ERIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "DR<<ANGELIKA", familyNameTransliterated = "MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }

    @Test
    fun `test holder match for doctor title (one whitespace) in different familyName`() {
        val covCertificate1 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "DR<<MUSTERMANN"),
            birthDate = "1980-02-03",
        )
        val covCertificate2 = CovCertificate(
            vaccinations = listOf(Vaccination()),
            name = Name(givenNameTransliterated = "ERIKA", familyNameTransliterated = "DR<<BEISPIELFRAU"),
            birthDate = "1980-02-03",
        )
        assertFalse(isHolderSame(covCertificate1, covCertificate2))
    }
}
