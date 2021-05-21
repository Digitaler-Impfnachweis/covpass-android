/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import de.rki.covpass.sdk.android.cert.models.CombinedVaccinationCertificate
import de.rki.covpass.sdk.android.cert.models.Name
import de.rki.covpass.sdk.android.cert.models.Vaccination
import de.rki.covpass.sdk.android.cert.models.VaccinationCertificate
import de.rki.covpass.sdk.android.cert.models.VaccinationCertificateList
import org.junit.Test
import java.time.LocalDate

internal class GroupedCertificateListTest {

    private val name1 = "Hans"
    private val name2 = "Franz"
    private val name3 = "Silke"
    private val date1 = LocalDate.of(1, 1, 1)
    private val date2 = LocalDate.of(2, 2, 2)
    private val date3 = LocalDate.of(3, 3, 3)
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
    private val certIncomplete1 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsIncomplete1,
    )
    private val certComplete1 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name1),
        birthDate = date1,
        vaccinations = vaccinationsComplete1,
    )
    private val certIncomplete2 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        vaccinations = vaccinationsIncomplete2,
    )
    private val certComplete2 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name2),
        birthDate = date2,
        vaccinations = vaccinationsComplete2,
    )
    private val certIncomplete3 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name3),
        birthDate = date3,
        vaccinations = vaccinationsIncomplete3,
    )
    private val certComplete3 = VaccinationCertificate(
        name = Name(familyNameTransliterated = name3),
        birthDate = date3,
        vaccinations = vaccinationsComplete3,
    )

    @Test
    fun `Empty VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {
        val originalList = VaccinationCertificateList()

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(0)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `One element VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(mutableListOf(toCombinedCert(certComplete1)))

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        )
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Three single elements VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(toCombinedCert(certComplete1), toCombinedCert(certComplete2), toCombinedCert(certComplete3))
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(3)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .getMainCertificate().vaccinationCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(1)
                .getMainCertificate().vaccinationCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates.get(2)
                .getMainCertificate().vaccinationCertificate.name.familyNameTransliterated
        )
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching element VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(toCombinedCert(certIncomplete1), toCombinedCert(certComplete1))
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Six matching element VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(1)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates.get(1)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates.get(2)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(
            groupedCertificatesList.certificates.get(2)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching and two single elements transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isNull()
        assertThat(
            groupedCertificatesList.certificates.get(1)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates.get(1)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name2)
        assertThat(
            groupedCertificatesList.certificates.get(2)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isNull()
        assertThat(
            groupedCertificatesList.certificates.get(2)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Ensure favoriteId is set to main certificate after transformation`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1)
            ),
            idIncomplete1
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .incompleteCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(
            groupedCertificatesList.certificates.get(0)
                .completeCertificate?.vaccinationCertificate?.name?.familyNameTransliterated
        ).isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isEqualTo(idComplete1)

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList.certificates).isEqualTo(originalList.certificates)
        assertThat(vaccinationCertificateList.favoriteCertId).isEqualTo(idComplete1)
    }

    @Test
    fun `getCombinedCertificate on empty list returns null`() {
        val emptyList = GroupedCertificatesList()
        assertThat(emptyList.getCombinedCertificate(idComplete1)).isNull()
    }

    @Test
    fun `getCombinedCertificate on full list returns correct results`() {
        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)

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
        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)

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

    @Test(expected = NoSuchElementException::class)
    fun `deleteVaccinationCertificate on empty list throws NoSuchElementException`() {
        val emptyList = GroupedCertificatesList()
        emptyList.deleteVaccinationCertificate(idComplete1)
    }

    @Test(expected = NoSuchElementException::class)
    fun `deleteVaccinationCertificate with non-existent id throws NoSuchElementException`() {
        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)

        groupedCertificatesList.deleteVaccinationCertificate(idComplete1)
    }

    @Test
    fun `deleteVaccinationCertificate for all elements results in empty list`() {
        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)

        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idIncomplete1))
            .isEqualTo(groupedCertificatesList.getGroupedCertificates(idComplete1)?.getMainCertId())
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idComplete1))
            .isNull()
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idComplete2))
            .isEqualTo(groupedCertificatesList.getGroupedCertificates(idIncomplete2)?.getMainCertId())
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idIncomplete2))
            .isNull()
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idIncomplete3))
            .isEqualTo(groupedCertificatesList.getGroupedCertificates(idComplete3)?.getMainCertId())
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idComplete3))
            .isNull()
        assertThat(groupedCertificatesList)
            .isEqualTo(GroupedCertificatesList())
    }

    @Test
    fun `deleteVaccinationCertificate for some elements results in partial list`() {
        val originalList = VaccinationCertificateList(
            mutableListOf(
                toCombinedCert(certIncomplete1),
                toCombinedCert(certComplete1),
                toCombinedCert(certIncomplete2),
                toCombinedCert(certComplete2),
                toCombinedCert(certIncomplete3),
                toCombinedCert(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)

        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idIncomplete1))
            .isEqualTo(groupedCertificatesList.getGroupedCertificates(idComplete1)?.getMainCertId())
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idComplete2))
            .isEqualTo(groupedCertificatesList.getGroupedCertificates(idIncomplete2)?.getMainCertId())
        assertThat(groupedCertificatesList.deleteVaccinationCertificate(idIncomplete2))
            .isNull()
        assertThat(groupedCertificatesList.certificates)
            .hasSize(2)
        assertThat(groupedCertificatesList.getGroupedCertificates(idIncomplete1))
            .isNull()
        assertThat(groupedCertificatesList.getGroupedCertificates(idComplete1)?.incompleteCertificate)
            .isNull()
        assertThat(groupedCertificatesList.getGroupedCertificates(idComplete1)?.completeCertificate)
            .isEqualTo(toCombinedCert(certComplete1))
        assertThat(groupedCertificatesList.getGroupedCertificates(idIncomplete2))
            .isNull()
        assertThat(groupedCertificatesList.getGroupedCertificates(idComplete2))
            .isNull()
        assertThat(groupedCertificatesList.getGroupedCertificates(idIncomplete3)?.incompleteCertificate)
            .isEqualTo(toCombinedCert(certIncomplete3))
        assertThat(groupedCertificatesList.getGroupedCertificates(idIncomplete3)?.completeCertificate)
            .isEqualTo(toCombinedCert(certComplete3))
        assertThat(groupedCertificatesList.getGroupedCertificates(idComplete3)?.incompleteCertificate)
            .isEqualTo(toCombinedCert(certIncomplete3))
        assertThat(groupedCertificatesList.getGroupedCertificates(idComplete3)?.completeCertificate)
            .isEqualTo(toCombinedCert(certComplete3))
    }

    private fun toCombinedCert(cert: VaccinationCertificate) =
        CombinedVaccinationCertificate(cert, "")
}
