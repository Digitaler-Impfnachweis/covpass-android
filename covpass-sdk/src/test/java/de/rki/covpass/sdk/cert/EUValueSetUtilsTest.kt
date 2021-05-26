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
    fun `validate country name`() {
        val countryCode = "DE"
        assertThat {
            getCountryName(countryCode)
        }.isSuccess().isEqualTo("Deutschland")
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
