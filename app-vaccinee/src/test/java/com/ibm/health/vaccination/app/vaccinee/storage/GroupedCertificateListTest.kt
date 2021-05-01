package com.ibm.health.vaccination.app.vaccinee.storage

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccination
import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificateList
import org.junit.Test
import java.time.LocalDate

internal class GroupedCertificateListTest {

    private val name1 = "Hans"
    private val name2 = "Franz"
    private val name3 = "Silke"
    private val date1 = LocalDate.of(1, 1, 1)
    private val date2 = LocalDate.of(2, 2, 2)
    private val date3 = LocalDate.of(3, 3, 3)
    private val vaccinationsIncomplete = listOf(ExtendedVaccination(series = "1/2"))
    private val vaccinationsComplete = listOf(ExtendedVaccination(series = "2/2"))
    private val certIncomplete1 = VaccinationCertificate(
        name = name1,
        birthDate = date1,
        vaccination = vaccinationsIncomplete,
        id = "certIncomplete1"
    )
    private val certComplete1 = VaccinationCertificate(
        name = name1,
        birthDate = date1,
        vaccination = vaccinationsComplete,
        id = "certComplete1"
    )
    private val certIncomplete2 = VaccinationCertificate(
        name = name2,
        birthDate = date2,
        vaccination = vaccinationsIncomplete,
        id = "certIncomplete2"
    )
    private val certComplete2 = VaccinationCertificate(
        name = name2,
        birthDate = date2,
        vaccination = vaccinationsComplete,
        id = "certComplete2"
    )
    private val certIncomplete3 = VaccinationCertificate(
        name = name3,
        birthDate = date3,
        vaccination = vaccinationsIncomplete,
        id = "certIncomplete3"
    )
    private val certComplete3 = VaccinationCertificate(
        name = name3,
        birthDate = date3,
        vaccination = vaccinationsComplete,
        id = "certComplete3"
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

        val originalList = VaccinationCertificateList(mutableListOf(extend(certComplete1)))

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(groupedCertificatesList.certificates.get(0).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Three single elements VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(extend(certComplete1), extend(certComplete2), extend(certComplete3))
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(3)
        assertThat(groupedCertificatesList.certificates.get(0).getMainCertificate().vaccinationCertificate.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(1).getMainCertificate().vaccinationCertificate.name)
            .isEqualTo(name2)
        assertThat(groupedCertificatesList.certificates.get(2).getMainCertificate().vaccinationCertificate.name)
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching element VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(mutableListOf(extend(certIncomplete1), extend(certComplete1)))

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(groupedCertificatesList.certificates.get(0).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(0).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId).isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Six matching element VaccinationCertificateList transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                extend(certIncomplete1),
                extend(certComplete1),
                extend(certIncomplete2),
                extend(certComplete2),
                extend(certIncomplete3),
                extend(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)
        assertThat(groupedCertificatesList.certificates.get(0).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(0).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(1).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name2)
        assertThat(groupedCertificatesList.certificates.get(1).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name2)
        assertThat(groupedCertificatesList.certificates.get(2).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.certificates.get(2).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Two matching and two single elements transformed to GroupedCertificatesList and backwards`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                extend(certIncomplete1),
                extend(certIncomplete2),
                extend(certComplete2),
                extend(certComplete3)
            )
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates)
            .hasSize(3)
        assertThat(groupedCertificatesList.certificates.get(0).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(0).completeCertificate?.vaccinationCertificate?.name)
            .isNull()
        assertThat(groupedCertificatesList.certificates.get(1).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name2)
        assertThat(groupedCertificatesList.certificates.get(1).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name2)
        assertThat(groupedCertificatesList.certificates.get(2).incompleteCertificate?.vaccinationCertificate?.name)
            .isNull()
        assertThat(groupedCertificatesList.certificates.get(2).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name3)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isNull()

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList).isEqualTo(originalList)
    }

    @Test
    fun `Ensure favoriteId is set to main certificate`() {

        val originalList = VaccinationCertificateList(
            mutableListOf(
                extend(certIncomplete1),
                extend(certComplete1)
            ),
            "certIncomplete1"
        )

        val groupedCertificatesList = GroupedCertificatesList.fromVaccinationCertificateList(originalList)
        assertThat(groupedCertificatesList.certificates).hasSize(1)
        assertThat(groupedCertificatesList.certificates.get(0).incompleteCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.certificates.get(0).completeCertificate?.vaccinationCertificate?.name)
            .isEqualTo(name1)
        assertThat(groupedCertificatesList.favoriteCertId)
            .isEqualTo("certComplete1")

        val vaccinationCertificateList = groupedCertificatesList.toVaccinationCertificateList()
        assertThat(vaccinationCertificateList.certificates).isEqualTo(originalList.certificates)
        assertThat(vaccinationCertificateList.favoriteCertId).isEqualTo("certComplete1")
    }

    private fun extend(cert: VaccinationCertificate) = ExtendedVaccinationCertificate(cert, "", null)
}
