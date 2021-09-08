/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.storage.EUValueSetRepository
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EUValueSetUtilsTest {

    @Test
    fun `validate manufacturers name`() {
        val rawManufacturersSets =
            EUValueSetRepository.vaccineManufacturer.valueSetValues

        assertEquals(
            rawManufacturersSets.map { it.value.display },
            rawManufacturersSets.map { getManufacturerName(it.key) }
        )
    }

    @Test
    fun `validate empty manufacturers name`() {
        val rawManufacturersName = ""
        assertEquals(rawManufacturersName, getManufacturerName(rawManufacturersName))
    }

    @Test
    fun `validate invalid manufacturers name`() {
        val rawManufacturersName = "ORG-100030215XYZ"
        assertEquals(rawManufacturersName, getManufacturerName(rawManufacturersName))
    }

    @Test
    fun `validate product name`() {
        val rawProductSets =
            EUValueSetRepository.vaccineMedicalProduct.valueSetValues

        assertEquals(rawProductSets.map { it.value.display }, rawProductSets.map { getProductName(it.key) })
    }

    @Test
    fun `validate empty product name`() {
        val rawProductName = ""
        assertEquals(rawProductName, getProductName(rawProductName))
    }

    @Test
    fun `validate invalid product name`() {
        val rawProductName = "EU/1/20/1528XYZ"
        assertEquals(rawProductName, getProductName(rawProductName))
    }

    @Test
    fun `validate prophylaxis name`() {
        val rawProphylaxisSets =
            EUValueSetRepository.vaccineProphylaxis.valueSetValues

        assertEquals(rawProphylaxisSets.map { it.value.display }, rawProphylaxisSets.map { getProphylaxisName(it.key) })
    }

    @Test
    fun `validate empty prophylaxis name`() {
        val rawProphylaxisName = ""
        assertEquals(rawProphylaxisName, getProphylaxisName(rawProphylaxisName))
    }

    @Test
    fun `validate invalid prophylaxis name`() {
        val rawProphylaxisName = "1119349007XYZ"
        assertEquals(rawProphylaxisName, getProphylaxisName(rawProphylaxisName))
    }

    @Test
    fun `validate test type name`() {
        val rawTestTypeSets =
            EUValueSetRepository.testType.valueSetValues

        assertEquals(rawTestTypeSets.map { it.value.display }, rawTestTypeSets.map { getTestTypeName(it.key) })
    }

    @Test
    fun `validate empty test type name`() {
        val rawTestTypeName = ""
        assertEquals(rawTestTypeName, getTestTypeName(rawTestTypeName))
    }

    @Test
    fun `validate invalid test type name`() {
        val rawTestTypeName = "1119349007XYZ"
        assertEquals(rawTestTypeName, getTestTypeName(rawTestTypeName))
    }

    @Test
    fun `validate test result name`() {
        val rawTestResultSets =
            EUValueSetRepository.testResult.valueSetValues

        assertEquals(rawTestResultSets.map { it.value.display }, rawTestResultSets.map { getTestResultName(it.key) })
    }

    @Test
    fun `validate empty test result name`() {
        val rawTestResultName = ""
        assertEquals(rawTestResultName, getTestResultName(rawTestResultName))
    }

    @Test
    fun `validate invalid test result name`() {
        val rawTestResultName = "1119349007XYZ"
        assertEquals(rawTestResultName, getTestResultName(rawTestResultName))
    }

    @Test
    fun `validate test manufacturer name`() {
        val rawTestManufacturerSets =
            EUValueSetRepository.testManufacturer.valueSetValues

        assertEquals(
            rawTestManufacturerSets.map { it.value.display },
            rawTestManufacturerSets.map { getTestManufacturerName(it.key) }
        )
    }

    @Test
    fun `validate empty test manufacturer name`() {
        val rawTestManufacturerName = ""
        assertEquals(rawTestManufacturerName, getTestManufacturerName(rawTestManufacturerName))
    }

    @Test
    fun `validate invalid test manufacturer name`() {
        val rawTestManufacturerName = "1119349007XYZ"
        assertEquals(rawTestManufacturerName, getTestManufacturerName(rawTestManufacturerName))
    }

    @Test
    fun `validate disease agent name`() {
        val rawDiseaseAgentSets =
            EUValueSetRepository.diseaseAgent.valueSetValues

        assertEquals(
            rawDiseaseAgentSets.map { it.value.display },
            rawDiseaseAgentSets.map { getDiseaseAgentName(it.key) }
        )
    }

    @Test
    fun `validate empty disease agent name`() {
        val rawDiseaseAgentName = ""
        assertEquals(rawDiseaseAgentName, getDiseaseAgentName(rawDiseaseAgentName))
    }

    @Test
    fun `validate invalid disease agent name`() {
        val rawDiseaseAgentName = "1119349007XYZ"
        assertEquals(rawDiseaseAgentName, getDiseaseAgentName(rawDiseaseAgentName))
    }

    @Test
    fun `validate country name`() {
        val countryCode = "DE"
        assertEquals("Germany", getCountryName(countryCode))
    }

    @Test
    fun `validate empty country name`() {
        val countryCode = ""
        assertEquals(countryCode, getCountryName(countryCode))
    }

    @Test
    fun `validate invalid country name`() {
        val countryCode = "XY"
        assertEquals(countryCode, getCountryName(countryCode))
    }
}
