/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import assertk.assertThat
import assertk.assertions.*
import org.junit.Test

internal class GroupedCertificateListTest {

    private val name1 = "Hans"
    private val name2 = "Franz"
    private val name3 = "Silke"
    private val date1 = "2021-01-01"
    private val date2 = "2021-02-02"
    private val date3 = "2021-03-03"
    private val idComplete1 = "certComplete1"
    private val idComplete2 = "certComplete2"
    private val idComplete3 = "certComplete3"
    private val idIncomplete1 = "certIncomplete1"
    private val idIncomplete2 = "certIncomplete2"
    private val idIncomplete3 = "certIncomplete3"
    private val vaccinationsIncomplete1 =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = idIncomplete1))
    private val vaccinationsComplete1 =
        listOf(Vaccination(doseNumber = 2, totalSerialDoses = 2, id = idComplete1))
    private val vaccinationsIncomplete2 =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = idIncomplete2))
    private val vaccinationsComplete2 =
        listOf(Vaccination(doseNumber = 2, totalSerialDoses = 2, id = idComplete2))
    private val vaccinationsIncomplete3 =
        listOf(Vaccination(doseNumber = 1, totalSerialDoses = 2, id = idIncomplete3))
    private val vaccinationsComplete3 =
        listOf(Vaccination(doseNumber = 2, totalSerialDoses = 2, id = idComplete3))
    private val certIncomplete1 = CovCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsIncomplete1,
    )
    private val certComplete1 = CovCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsComplete1,
    )
    private val certIncomplete2 = CovCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        vaccinations = vaccinationsIncomplete2,
    )
    private val certComplete2 = CovCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        vaccinations = vaccinationsComplete2,
    )
    private val certIncomplete3 = CovCertificate(
        name = Name(familyNameTransliterated = name3),
        birthDate = date3,
        vaccinations = vaccinationsIncomplete3,
    )
    private val certComplete3 = CovCertificate(
        name = Name(familyNameTransliterated = name3),
        birthDate = date3,
        vaccinations = vaccinationsComplete3,
    )

    @Test
    fun `Empty CovCertificateList transformed to GroupedCertificatesList and backwards`() {
        val originalList = CovCertificateList()

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(0)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `One element CovCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = CovCertificateList(mutableListOf(toCombinedCert(certComplete1)))

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Three single elements CovCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = CovCertificateList(
            mutableListOf(toCombinedCert(certComplete1), toCombinedCert(certComplete2), toCombinedCert(certComplete3))
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(3)
        assertThat(
            groupedCertificatesList.certificates[0]
                .getMainCertificate().covCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates[1]
                .getMainCertificate().covCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates[2]
                .getMainCertificate().covCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching element CovCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = CovCertificateList(
            mutableListOf(toCombinedCert(certIncomplete1), toCombinedCert(certComplete1))
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Six matching element CovCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates[1].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates[1].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates[2].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(
            groupedCertificatesList.certificates[2].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching and two single elements transformed to GroupedCertificatesList and backwards`() {

        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)

        assertThat(
            groupedCertificatesList.certificates[0].certificates
        ).isNotNull().hasSize(1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)

        assertThat(
            groupedCertificatesList.certificates[1].certificates
        ).isNotNull().hasSize(2)
        assertThat(
            groupedCertificatesList.certificates[1].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates[1].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name2)

        assertThat(
            groupedCertificatesList.certificates[2].certificates
        ).isNotNull().hasSize(1)
        assertThat(
            groupedCertificatesList.certificates[2].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Ensure favoriteId is set to main certificate after transformation`() {
        val testId = GroupedCertificatesId(Name(name1), birthDate = "2011-11-11")

        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1)
            ),
            testId
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates[0].certificates[1].covCertificate.name.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isEqualTo(testId)

        val covCertificateList = groupedCertificatesList.toCovCertificateList()
        assertThat(covCertificateList.certificates).isEqualTo(originalList.certificates)
        assertThat(covCertificateList.favoriteCertId).isEqualTo(testId)
    }

    @Test
    fun `getCombinedCertificate on empty list returns null`() {
        val emptyList = GroupedCertificatesList()
        assertThat(emptyList.getCombinedCertificate(idComplete1)).isNull()
    }

    @Test
    fun `getCombinedCertificate on full list returns correct results`() {
        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)

        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete1))
            .isEqualTo(toCombinedCert(certIncomplete1))
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete1))
            .isEqualTo(toCombinedCert(certComplete1))
        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete2))
            .isEqualTo(toCombinedCert(certIncomplete2))
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete2))
            .isEqualTo(toCombinedCert(certComplete2))
        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete3))
            .isEqualTo(toCombinedCert(certIncomplete3))
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete3))
            .isEqualTo(toCombinedCert(certComplete3))
    }

    @Test
    fun `getCombinedCertificate on half filled list returns correct results`() {
        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)

        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete1))
            .isEqualTo(toCombinedCert(certIncomplete1))
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete1))
            .isNull()
        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete2))
            .isNull()
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete2))
            .isEqualTo(toCombinedCert(certComplete2))
        assertThat(groupedCertificatesList.getCombinedCertificate(idIncomplete3))
            .isEqualTo(toCombinedCert(certIncomplete3))
        assertThat(groupedCertificatesList.getCombinedCertificate(idComplete3))
            .isEqualTo(toCombinedCert(certComplete3))
    }

    @Test
    fun `deleteCovCertificate on empty list returns false`() {
        val emptyList = GroupedCertificatesList()
        assertThat(emptyList.deleteCovCertificate(idComplete1)).isFalse()
    }

    @Test
    fun `deleteCovCertificate with non-existent id returns false`() {
        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)

        assertThat(groupedCertificatesList.deleteCovCertificate(idComplete1)).isFalse()
    }

    @Test
    fun `deleteCovCertificate for all elements results in empty list`() {
        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)

        assertThat(groupedCertificatesList.deleteCovCertificate(idIncomplete1)).isFalse()
        assertThat(groupedCertificatesList.deleteCovCertificate(idComplete1)).isTrue()
        assertThat(groupedCertificatesList.deleteCovCertificate(idComplete2)).isFalse()
        assertThat(groupedCertificatesList.deleteCovCertificate(idIncomplete2)).isTrue()
        assertThat(groupedCertificatesList.deleteCovCertificate(idIncomplete3)).isFalse()
        assertThat(groupedCertificatesList.deleteCovCertificate(idComplete3)).isTrue()
        assertThat(groupedCertificatesList).isEqualTo(GroupedCertificatesList())
    }

    @Test
    fun `deleteCovCertificate for some elements results in partial list`() {
        val originalList = CovCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)

        assertThat(groupedCertificatesList.deleteCovCertificate(idIncomplete1))
            .isFalse()
        assertThat(groupedCertificatesList.deleteCovCertificate(idComplete2))
            .isFalse()
        assertThat(groupedCertificatesList.deleteCovCertificate(idIncomplete2))
            .isTrue()
        assertThat(groupedCertificatesList.certificates)
            .hasSize(2)

        val certificatesIncomplete1 = groupedCertificatesList.getGroupedCertificates(
            GroupedCertificatesId(certIncomplete1.name, certIncomplete1.birthDate)
        )
        val certificatesComplete1 = groupedCertificatesList.getGroupedCertificates(
            GroupedCertificatesId(certComplete1.name, certComplete1.birthDate)
        )
        assertThat(certificatesIncomplete1).isEqualTo(certificatesComplete1)
        assertThat(certificatesComplete1?.certificates)
            .isNotNull().hasSize(1)
        assertThat(certificatesComplete1?.certificates?.get(0))
            .isEqualTo(toCombinedCert(certComplete1))

        assertThat(
            groupedCertificatesList.getGroupedCertificates(
                GroupedCertificatesId(certIncomplete2.name, certIncomplete2.birthDate)
            )
        ).isNull()
        assertThat(
            groupedCertificatesList.getGroupedCertificates(
                GroupedCertificatesId(certComplete2.name, certComplete2.birthDate)
            )
        ).isNull()

        val certificatesIncomplete3 = groupedCertificatesList.getGroupedCertificates(
            GroupedCertificatesId(certIncomplete3.name, certIncomplete3.birthDate)
        )
        val certificatesComplete3 = groupedCertificatesList.getGroupedCertificates(
            GroupedCertificatesId(certComplete3.name, certComplete3.birthDate)
        )
        assertThat(certificatesIncomplete3).isEqualTo(certificatesComplete3)
        assertThat(certificatesComplete3?.certificates)
            .isNotNull().hasSize(2)
        assertThat(certificatesComplete3?.certificates?.get(0))
            .isEqualTo(toCombinedCert(certIncomplete3))
        assertThat(certificatesComplete3?.certificates?.get(1))
            .isEqualTo(toCombinedCert(certComplete3))
    }

    @Test
    fun `addNewCertificate to empty list and check favoriteCertId`() {
        val originalList = CovCertificateList(
            mutableListOf()
        )
        val groupedCertificatesList = GroupedCertificatesList.fromCovCertificateList(originalList)
        assertThat(
            groupedCertificatesList.favoriteCertId
        ).isEqualTo(null)

        groupedCertificatesList.addNewCertificate(toCombinedCert(certIncomplete1))
        assertThat(
            groupedCertificatesList.favoriteCertId
        ).isEqualTo(
            GroupedCertificatesId(certIncomplete1.name, certIncomplete1.birthDate)
        )

        groupedCertificatesList.addNewCertificate(toCombinedCert(certIncomplete2))
        assertThat(
            groupedCertificatesList.certificates[0].certificates[0].covCertificate.dgcEntry.id
        ).isEqualTo(idIncomplete1)
        assertThat(
            groupedCertificatesList.certificates[1].certificates[0].covCertificate.dgcEntry.id
        ).isEqualTo(idIncomplete2)
    }

    private fun toCombinedCert(cert: CovCertificate) =
        CombinedCovCertificate(cert, "")
}
