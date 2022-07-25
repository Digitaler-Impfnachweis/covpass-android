/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GroupedCertificatesTest {

    private val givenName = "Hans"
    private val familyName = "Mustermann"
    private val date = "2021-01-01"
    private val idComplete = "certComplete"
    private val idCompleteSingleDose = "certCompleteSingleDose"
    private val idCompleteSingleDoseJansen = "certCompleteSingleDoseJansen"
    private val idRecovery = "certRecovery"
    private val recovery =
        listOf(
            Recovery(
                id = idRecovery,
                firstResult = LocalDate.of(2021, 6, 10)
            )
        )
    private val vaccinationCompleteSingleDose =
        listOf(
            Vaccination(
                doseNumber = 1,
                totalSerialDoses = 1,
                id = idCompleteSingleDose,
                product = "",
                occurrence = LocalDate.of(2021, 6, 11)
            )
        )
    private val vaccinationCompleteSingleDoseJansen =
        listOf(
            Vaccination(
                doseNumber = 1,
                totalSerialDoses = 1,
                id = idCompleteSingleDoseJansen,
                product = "EU/1/20/1525",
                occurrence = LocalDate.of(2021, 6, 12)
            )
        )
    private val vaccinationComplete =
        listOf(
            Vaccination(
                doseNumber = 2,
                totalSerialDoses = 2,
                id = idComplete,
                occurrence = LocalDate.of(2021, 6, 13)
            )
        )

    private val certCompleteSingleDose = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        vaccinations = vaccinationCompleteSingleDose,
        validUntil = Instant.now(),
        issuer = "DE"
    )
    private val certCompleteSingleDoseJansen = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        vaccinations = vaccinationCompleteSingleDoseJansen,
        validUntil = Instant.now(),
        issuer = "DE"
    )
    private val certComplete = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        vaccinations = vaccinationComplete,
        validUntil = Instant.now(),
        issuer = "DE"
    )
    private val certRecovery = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        recoveries = recovery,
        validUntil = Instant.now(),
        issuer = "DE"
    )
    private val combinedCovCertComplete = certComplete.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)
    private val combinedCovCertCompleteSingleDose = certCompleteSingleDose.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)
    private val combinedCovCertCompleteSingleDoseJansen = certCompleteSingleDoseJansen.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)
    private val combinedCovCertRecovery = certRecovery.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)

    @Test
    fun `test isCertVaccinationNotBoosterAfterJanssen`() {
        val groupedCertificates = GroupedCertificates(
            certificates = mutableListOf(
                combinedCovCertComplete,
                combinedCovCertCompleteSingleDose,
                combinedCovCertRecovery
            )
        )
        assertEquals(
            false,
            groupedCertificates.isCertVaccinationNotBoosterAfterJanssen(
                certComplete
            )
        )

        val groupedCertificates2 = GroupedCertificates(
            certificates = mutableListOf(
                combinedCovCertComplete,
                combinedCovCertCompleteSingleDoseJansen,
            )
        )
        assertEquals(
            true,
            groupedCertificates2.isCertVaccinationNotBoosterAfterJanssen(
                certComplete
            )
        )

        val groupedCertificates3 = GroupedCertificates(
            certificates = mutableListOf(
                combinedCovCertComplete,
                combinedCovCertCompleteSingleDoseJansen,
                combinedCovCertRecovery
            )
        )
        assertEquals(
            false,
            groupedCertificates3.isCertVaccinationNotBoosterAfterJanssen(
                certComplete
            )
        )
    }

    @Test
    fun `test getHistoricalDataForDcc`() {

        val groupedCertificates = GroupedCertificates(
            certificates = mutableListOf(
                combinedCovCertComplete,
                combinedCovCertCompleteSingleDoseJansen,
                combinedCovCertRecovery
            )
        )
        assertEquals(
            listOf(idRecovery, idCompleteSingleDoseJansen),
            groupedCertificates.getHistoricalDataForDcc(
                idComplete
            )
        )
    }

    @Test
    fun `test getListOfImportantCerts`() {
        val groupedCertificates = GroupedCertificates(
            certificates = mutableListOf(
                combinedCovCertComplete,
                combinedCovCertCompleteSingleDoseJansen,
                combinedCovCertRecovery
            )
        )
        assertEquals(
            listOf(idComplete, idRecovery),
            groupedCertificates.getListOfImportantCerts()
        )
    }

    private fun CovCertificate.toCombinedCertLocal(qrContent: String = "") =
        CombinedCovCertificateLocal(this, qrContent)
}
