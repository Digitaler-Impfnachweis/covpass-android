/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.cert.models.TestCert.Companion.ANTIGEN_TEST
import de.rki.covpass.sdk.cert.models.TestCert.Companion.NEGATIVE_RESULT
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TicketingTest {

    private val qrCoder: QRCoder = mockk(relaxed = true)
    private val mapper = CertificateListMapper(qrCoder)

    private val name1 = "name1"
    private val name2 = "name2"
    private val name3 = "name3"
    private val givenName3 = "givenName3"
    private val date1 = "2021-01-01"
    private val date2 = "2021-02-02"
    private val date3 = "2021-03-03"
    private val idComplete = "certComplete"
    private val idIncomplete = "certIncomplete"
    private val idRecovery = "certRecovery"
    private val idTestPcr = "certTestPcr"
    private val idTestAntigen = "certTestAntigen"
    private val vaccinationsIncomplete =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = idIncomplete))
    private val vaccinationsComplete =
        listOf(Vaccination(doseNumber = 2, totalSerialDoses = 2, id = idComplete))
    private val recovery =
        listOf(Recovery(id = idRecovery))
    private val testPcr =
        listOf(TestCert(id = idTestPcr, testResult = NEGATIVE_RESULT, testType = PCR_TEST))
    private val testAntigen =
        listOf(TestCert(id = idTestAntigen, testResult = NEGATIVE_RESULT, testType = ANTIGEN_TEST))

    private val certVaccinationIncomplete = CovCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsIncomplete,
        validUntil = Instant.now()
    )
    private val certVaccinationComplete = CovCertificate(
        name = Name(familyNameTransliterated = name3, givenNameTransliterated = givenName3),
        birthDate = date3,
        vaccinations = vaccinationsComplete,
        validUntil = Instant.now()
    )
    private val certRecovery = CovCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        recoveries = recovery,
        validUntil = Instant.now()
    )
    private val certTestPcr = CovCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        tests = testPcr,
        validUntil = Instant.now()
    )
    private val certTestAntigen = CovCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        tests = testAntigen,
        validUntil = Instant.now()
    )

    private val originalList by lazy {
        CovCertificateList(
            mutableListOf(
                certVaccinationIncomplete.toCombinedCertLocal(),
                certVaccinationComplete.toCombinedCertLocal(),
                certRecovery.toCombinedCertLocal(),
                certTestPcr.toCombinedCertLocal(),
                certTestAntigen.toCombinedCertLocal(),
            )
        )
    }

    private val groupedCertificatesList by lazy {
        mapper.toGroupedCertificatesList(originalList)
    }

    @Test
    fun `filter generic test for name1 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Test.Generic
            ),
            firstName = null,
            lastName = name1,
            date1
        )
        assert(filteredCertificates.size == 1)
        assertEquals(
            filteredCertificates[0],
            certTestPcr.toCombinedCertLocal().toCombinedCovCertificate(CertValidationResult.Expired)
        )
    }

    @Test
    fun `filter PCR test for name1 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Test.Pcr
            ),
            firstName = null,
            lastName = name1,
            date1
        )
        assert(filteredCertificates.size == 1)
        assertEquals(
            filteredCertificates[0],
            certTestPcr.toCombinedCertLocal().toCombinedCovCertificate(CertValidationResult.Expired)
        )
    }

    @Test
    fun `filter vaccination for name1 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Vaccination
            ),
            firstName = null,
            lastName = name1,
            date1
        )
        assert(filteredCertificates.size == 1)
        assertEquals(
            filteredCertificates[0],
            certVaccinationIncomplete.toCombinedCertLocal().toCombinedCovCertificate(CertValidationResult.Expired)
        )
    }

    @Test
    fun `filter recovery for name2 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Recovery
            ),
            firstName = null,
            lastName = name2,
            date2
        )
        assert(filteredCertificates.size == 1)
        assertEquals(
            filteredCertificates[0],
            certRecovery.toCombinedCertLocal().toCombinedCovCertificate(CertValidationResult.Expired)
        )
    }

    @Test
    fun `filter vaccination for name2 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Vaccination
            ),
            firstName = null,
            lastName = name2,
            date2
        )
        assert(filteredCertificates.isEmpty())
    }

    @Test
    fun `filter vaccination for name3 and givenName3 certs`() {
        val filteredCertificates = groupedCertificatesList.filterCertificates(
            types = listOf(
                TicketingType.Vaccination
            ),
            firstName = givenName3,
            lastName = name3,
            date3
        )
        assert(filteredCertificates.size == 1)
        assertEquals(
            filteredCertificates[0],
            certVaccinationComplete.toCombinedCertLocal().toCombinedCovCertificate(CertValidationResult.Expired)
        )
    }

    private fun CovCertificate.toCombinedCertLocal(qrContent: String = "") =
        CombinedCovCertificateLocal(this, qrContent)
}
