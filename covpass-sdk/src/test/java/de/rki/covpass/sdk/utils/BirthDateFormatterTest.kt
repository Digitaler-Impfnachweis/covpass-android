/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Vaccination
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BirthDateFormatterTest {

    private val cert: CovCertificate = CovCertificate(
        vaccinations = listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = "")),
    )

    @Test
    fun `test valid birth date format XXXX-XX-XX`() {
        val covCertificate = cert.copy(birthDate = "2021-03-15")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }

    @Test
    fun `test valid birth date format XXXX-XX`() {
        val covCertificate = cert.copy(birthDate = "2021-03")
        assertEquals("${covCertificate.birthDate}-XX", covCertificate.birthDateFormatted)
    }

    @Test
    fun `test valid birth date format XXXX`() {
        val covCertificate = cert.copy(birthDate = "2021")
        assertEquals("${covCertificate.birthDate}-XX-XX", covCertificate.birthDateFormatted)
    }

    @Test
    fun `test empty birth date`() {
        val covCertificate = cert.copy(birthDate = "")
        assertEquals("XXXX-XX-XX", covCertificate.birthDateFormatted)
    }

    @Test
    fun `test invalid birth date format XX-XX-XXXX`() {
        val covCertificate = cert.copy(birthDate = "15-03-2021")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }

    @Test
    fun `test invalid birth date format XX-XXXX`() {
        val covCertificate = cert.copy(birthDate = "03-2021")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }

    @Test
    fun `test invalid birth date format`() {
        val covCertificate = cert.copy(birthDate = "15.03.2021")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }

    @Test
    fun `test invalid birth date format 123a`() {
        val covCertificate = cert.copy(birthDate = "123a")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }

    @Test
    fun `test invalid birth date format 'unknown date'`() {
        val covCertificate = cert.copy(birthDate = "unknown date")
        assertEquals(covCertificate.birthDate, covCertificate.birthDateFormatted)
    }
}
