/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getBoosterAfterVaccinationAfterRecoveryIds
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CertificateReissueUtilsTest {

    private val givenName = "Hans"
    private val familyName = "Mustermann"
    private val date = "2021-01-01"
    private val idComplete = "certComplete1"
    private val idCompleteSingleDose = "certCompleteSingleDose1"
    private val idRecovery = "certRecovery"
    private val recovery =
        listOf(Recovery(id = idRecovery))
    private val vaccinationCompleteSingleDose =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 1, id = idCompleteSingleDose))
    private val vaccinationComplete =
        listOf(Vaccination(doseNumber = 2, totalSerialDoses = 2, id = idComplete))

    private val certCompleteSingleDose = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        vaccinations = vaccinationCompleteSingleDose,
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
    private val certRecovery1 = CovCertificate(
        name = Name(familyNameTransliterated = familyName, givenNameTransliterated = givenName),
        birthDate = date,
        recoveries = recovery,
        validUntil = Instant.now(),
        issuer = "DE"
    )
    private val combinedCovCertComplete1 = certComplete.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)
    private val combinedCovCertCompleteSingleDose1 = certCompleteSingleDose.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)
    private val combinedCovCertRecovery1 = certRecovery1.toCombinedCertLocal()
        .toCombinedCovCertificate(CertValidationResult.Valid)

    @Test
    fun `test getBoosterAfterVaccinationAfterRecoveryIds`() {
        val result = getBoosterAfterVaccinationAfterRecoveryIds(
            listOf(combinedCovCertComplete1, combinedCovCertCompleteSingleDose1)
        )
        assertEquals(listOf(idCompleteSingleDose, idComplete), result)

        val result2 = getBoosterAfterVaccinationAfterRecoveryIds(
            listOf(combinedCovCertComplete1, combinedCovCertCompleteSingleDose1, combinedCovCertRecovery1)
        )
        assertEquals(listOf(idRecovery, idCompleteSingleDose, idComplete), result2)
    }

    private fun CovCertificate.toCombinedCertLocal(qrContent: String = "") =
        CombinedCovCertificateLocal(this, qrContent)
}
