/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Vaccination
import org.junit.Test

internal class BirthDateFormatterTest {

    private val cert: CovCertificate = CovCertificate(
        vaccinations = listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = ""))
    )

    @Test
    fun `test valid birth date format XXXX-XX-XX`() {
        val covCertificate = cert.copy(birthDate = "2021-03-15")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }

    @Test
    fun `test valid birth date format XXXX-XX`() {
        val covCertificate = cert.copy(birthDate = "2021-03")
        assertThat(covCertificate.birthDateFormatted).isEqualTo("${covCertificate.birthDate}-XX")
    }

    @Test
    fun `test valid birth date format XXXX`() {
        val covCertificate = cert.copy(birthDate = "2021")
        assertThat(covCertificate.birthDateFormatted).isEqualTo("${covCertificate.birthDate}-XX-XX")
    }

    @Test
    fun `test empty birth date`() {
        val covCertificate = cert.copy(birthDate = "")
        assertThat(covCertificate.birthDateFormatted).isEqualTo("XXXX-XX-XX")
    }

    @Test
    fun `test invalid birth date format XX-XX-XXXX`() {
        val covCertificate = cert.copy(birthDate = "15-03-2021")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }

    @Test
    fun `test invalid birth date format XX-XXXX`() {
        val covCertificate = cert.copy(birthDate = "03-2021")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }

    @Test
    fun `test invalid birth date format`() {
        val covCertificate = cert.copy(birthDate = "15.03.2021")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }

    @Test
    fun `test invalid birth date format 123a`() {
        val covCertificate = cert.copy(birthDate = "123a")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }

    @Test
    fun `test invalid birth date format 'unknown date'`() {
        val covCertificate = cert.copy(birthDate = "unknown date")
        assertThat(covCertificate.birthDateFormatted).isEqualTo(covCertificate.birthDate)
    }
}
