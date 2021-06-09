/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import de.rki.covpass.sdk.storage.EUValueSetRepository
import org.junit.Test

internal class EUValueSetUtilsTest {

    @Test
    fun `validate manufacturers name`() {
        val rawManufacturersSets =
            EUValueSetRepository.vaccineManufacturer.valueSetValues

        assertThat {
            rawManufacturersSets.map { getManufacturerName(it.key) }
        }.isSuccess().isEqualTo(
            rawManufacturersSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty manufacturers name`() {
        val rawManufacturersName = ""
        assertThat {
            getManufacturerName(rawManufacturersName)
        }.isSuccess().isEqualTo(rawManufacturersName)
    }

    @Test
    fun `validate invalid manufacturers name`() {
        val rawManufacturersName = "ORG-100030215XYZ"
        assertThat {
            getManufacturerName(rawManufacturersName)
        }.isSuccess().isEqualTo(rawManufacturersName)
    }

    @Test
    fun `validate product name`() {
        val rawProductSets =
            EUValueSetRepository.vaccineMedicalProduct.valueSetValues

        assertThat {
            rawProductSets.map { getProductName(it.key) }
        }.isSuccess().isEqualTo(
            rawProductSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty product name`() {
        val rawProductName = ""
        assertThat {
            getProductName(rawProductName)
        }.isSuccess().isEqualTo(rawProductName)
    }

    @Test
    fun `validate invalid product name`() {
        val rawProductName = "EU/1/20/1528XYZ"
        assertThat {
            getProductName(rawProductName)
        }.isSuccess().isEqualTo(rawProductName)
    }

    @Test
    fun `validate prophylaxis name`() {
        val rawProphylaxisSets =
            EUValueSetRepository.vaccineProphylaxis.valueSetValues

        assertThat {
            rawProphylaxisSets.map { getProphylaxisName(it.key) }
        }.isSuccess().isEqualTo(
            rawProphylaxisSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty prophylaxis name`() {
        val rawProphylaxisName = ""
        assertThat {
            getProphylaxisName(rawProphylaxisName)
        }.isSuccess().isEqualTo(rawProphylaxisName)
    }

    @Test
    fun `validate invalid prophylaxis name`() {
        val rawProphylaxisName = "1119349007XYZ"
        assertThat {
            getProphylaxisName(rawProphylaxisName)
        }.isSuccess().isEqualTo(rawProphylaxisName)
    }

    @Test
    fun `validate test type name`() {
        val rawTestTypeSets =
            EUValueSetRepository.testType.valueSetValues

        assertThat {
            rawTestTypeSets.map { getTestTypeName(it.key) }
        }.isSuccess().isEqualTo(
            rawTestTypeSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty test type name`() {
        val rawTestTypeName = ""
        assertThat {
            getTestTypeName(rawTestTypeName)
        }.isSuccess().isEqualTo(rawTestTypeName)
    }

    @Test
    fun `validate invalid test type name`() {
        val rawTestTypeName = "1119349007XYZ"
        assertThat {
            getTestTypeName(rawTestTypeName)
        }.isSuccess().isEqualTo(rawTestTypeName)
    }

    @Test
    fun `validate test result name`() {
        val rawTestResultSets =
            EUValueSetRepository.testResult.valueSetValues

        assertThat {
            rawTestResultSets.map { getTestResultName(it.key) }
        }.isSuccess().isEqualTo(
            rawTestResultSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty test result name`() {
        val rawTestResultName = ""
        assertThat {
            getTestResultName(rawTestResultName)
        }.isSuccess().isEqualTo(rawTestResultName)
    }

    @Test
    fun `validate invalid test result name`() {
        val rawTestResultName = "1119349007XYZ"
        assertThat {
            getTestResultName(rawTestResultName)
        }.isSuccess().isEqualTo(rawTestResultName)
    }

    @Test
    fun `validate test manufacturer name`() {
        val rawTestManufacturerSets =
            EUValueSetRepository.testManufacturer.valueSetValues

        assertThat {
            rawTestManufacturerSets.map { getTestManufacturerName(it.key) }
        }.isSuccess().isEqualTo(
            rawTestManufacturerSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty test manufacturer name`() {
        val rawTestManufacturerName = ""
        assertThat {
            getTestManufacturerName(rawTestManufacturerName)
        }.isSuccess().isEqualTo(rawTestManufacturerName)
    }

    @Test
    fun `validate invalid test manufacturer name`() {
        val rawTestManufacturerName = "1119349007XYZ"
        assertThat {
            getTestManufacturerName(rawTestManufacturerName)
        }.isSuccess().isEqualTo(rawTestManufacturerName)
    }

    @Test
    fun `validate disease agent name`() {
        val rawDiseaseAgentSets =
            EUValueSetRepository.diseaseAgent.valueSetValues

        assertThat {
            rawDiseaseAgentSets.map { getDiseaseAgentName(it.key) }
        }.isSuccess().isEqualTo(
            rawDiseaseAgentSets.map { it.value.display }
        )
    }

    @Test
    fun `validate empty disease agent name`() {
        val rawDiseaseAgentName = ""
        assertThat {
            getDiseaseAgentName(rawDiseaseAgentName)
        }.isSuccess().isEqualTo(rawDiseaseAgentName)
    }

    @Test
    fun `validate invalid disease agent name`() {
        val rawDiseaseAgentName = "1119349007XYZ"
        assertThat {
            getDiseaseAgentName(rawDiseaseAgentName)
        }.isSuccess().isEqualTo(rawDiseaseAgentName)
    }

    @Test
    fun `validate country name`() {
        val countryCode = "DE"
        assertThat {
            getCountryName(countryCode)
        }.isSuccess().isEqualTo("Germany")
    }

    @Test
    fun `validate empty country name`() {
        val countryCode = ""
        assertThat {
            getCountryName(countryCode)
        }.isSuccess().isEqualTo(countryCode)
    }

    @Test
    fun `validate invalid country name`() {
        val countryCode = "XY"
        assertThat {
            getCountryName(countryCode)
        }.isSuccess().isEqualTo(countryCode)
    }
}
