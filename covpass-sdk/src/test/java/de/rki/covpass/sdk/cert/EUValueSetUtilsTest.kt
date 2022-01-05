/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassValueSet
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.local.valuesets.toEuValueSet
import de.rki.covpass.sdk.rules.remote.valuesets.CovPassValueSetRemote
import de.rki.covpass.sdk.rules.remote.valuesets.toCovPassValueSet
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.readTextAssetFromTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DependencyAccessor::class)
internal class EUValueSetUtilsTest {

    private val remoteDataSource: CovPassValueSetsRemoteDataSource = mockk(relaxed = true)
    private val localDataSource: CovPassValueSetsLocalDataSource = mockk(relaxed = true)
    private val rulesUpdateRepository: RulesUpdateRepository = mockk(relaxed = true)
    private val covPassValueSetsRepository: CovPassValueSetsRepository by lazy {
        CovPassValueSetsRepository(remoteDataSource, localDataSource, rulesUpdateRepository)
    }
    private val euValueSetsPath: String by lazy { "covpass-sdk/eu-value-sets.json" }
    private val covPassValueSetsRemote: List<CovPassValueSetRemote> by lazy {
        defaultJson.decodeFromString(
            readTextAssetFromTest(euValueSetsPath)
        )
    }

    private val covPassValueSets: List<CovPassValueSet> by lazy {
        covPassValueSetsRemote.map { it.toCovPassValueSet("") }
    }

    private val covPassValueSetsLocal: List<CovPassValueSetLocal> by lazy {
        covPassValueSets.map { it.toCovPassValueSetLocal() }
    }

    private val euValueSets by lazy {
        covPassValueSetsLocal.map { it.toEuValueSet() }
    }

    init {
        coEvery {
            localDataSource.getAll()
        } returns covPassValueSetsLocal
        sdkDeps = mockk(relaxed = true)
        every { sdkDeps.covPassValueSetsRepository } returns covPassValueSetsRepository
    }

    @Test
    fun `validate manufacturers name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "vaccines-covid-19-auth-holders" }?.valueSetValues?.map {
                it.value.display
            },
            covPassValueSetsRepository.getVaccineManufacturer()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getManufacturerName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty manufacturers name`() {
        val rawManufacturersName = ""
        assertEquals(rawManufacturersName, covPassValueSetsRepository.getManufacturerName(rawManufacturersName))
    }

    @Test
    fun `validate invalid manufacturers name`() {
        val rawManufacturersName = "ORG-100030215XYZ"
        assertEquals(rawManufacturersName, covPassValueSetsRepository.getManufacturerName(rawManufacturersName))
    }

    @Test
    fun `validate product name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "vaccines-covid-19-names" }?.valueSetValues?.map { it.value.display },
            covPassValueSetsRepository.getVaccineMedicalProduct()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getProductName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty product name`() {
        val rawProductName = ""
        assertEquals(rawProductName, covPassValueSetsRepository.getProductName(rawProductName))
    }

    @Test
    fun `validate invalid product name`() {
        val rawProductName = "EU/1/20/1528XYZ"
        assertEquals(rawProductName, covPassValueSetsRepository.getProductName(rawProductName))
    }

    @Test
    fun `validate prophylaxis name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "sct-vaccines-covid-19" }?.valueSetValues?.map { it.value.display },
            covPassValueSetsRepository.getVaccineProphylaxis()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getManufacturerName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty prophylaxis name`() {
        val rawProphylaxisName = ""
        assertEquals(rawProphylaxisName, covPassValueSetsRepository.getProphylaxisName(rawProphylaxisName))
    }

    @Test
    fun `validate invalid prophylaxis name`() {
        val rawProphylaxisName = "1119349007XYZ"
        assertEquals(rawProphylaxisName, covPassValueSetsRepository.getProphylaxisName(rawProphylaxisName))
    }

    @Test
    fun `validate test type name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "covid-19-lab-test-type" }?.valueSetValues?.map { it.value.display },
            covPassValueSetsRepository.getTestType()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getTestTypeName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty test type name`() {
        val rawTestTypeName = ""
        assertEquals(rawTestTypeName, covPassValueSetsRepository.getTestTypeName(rawTestTypeName))
    }

    @Test
    fun `validate invalid test type name`() {
        val rawTestTypeName = "1119349007XYZ"
        assertEquals(rawTestTypeName, covPassValueSetsRepository.getTestTypeName(rawTestTypeName))
    }

    @Test
    fun `validate test result name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "covid-19-lab-result" }?.valueSetValues?.map { it.value.display },
            covPassValueSetsRepository.getTestResult()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getTestResultName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty test result name`() {
        val rawTestResultName = ""
        assertEquals(rawTestResultName, covPassValueSetsRepository.getTestResultName(rawTestResultName))
    }

    @Test
    fun `validate invalid test result name`() {
        val rawTestResultName = "1119349007XYZ"
        assertEquals(rawTestResultName, covPassValueSetsRepository.getTestResultName(rawTestResultName))
    }

    @Test
    fun `validate test manufacturer name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "covid-19-lab-test-manufacturer-and-name" }?.valueSetValues?.map {
                it.value.display
            },
            covPassValueSetsRepository.getTestManufacturer()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getTestManufacturerName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty test manufacturer name`() {
        val rawTestManufacturerName = ""
        assertEquals(
            rawTestManufacturerName,
            covPassValueSetsRepository.getTestManufacturerName(rawTestManufacturerName)
        )
    }

    @Test
    fun `validate invalid test manufacturer name`() {
        val rawTestManufacturerName = "1119349007XYZ"
        assertEquals(
            rawTestManufacturerName,
            covPassValueSetsRepository.getTestManufacturerName(rawTestManufacturerName)
        )
    }

    @Test
    fun `validate disease agent name`() {
        assertEquals(
            euValueSets.find { it.valueSetId == "disease-agent-targeted" }?.valueSetValues?.map { it.value.display },
            covPassValueSetsRepository.getDiseaseAgent()?.toEuValueSet()?.valueSetValues?.map {
                covPassValueSetsRepository.getDiseaseAgentName(it.value.display)
            }
        )
    }

    @Test
    fun `validate empty disease agent name`() {
        val rawDiseaseAgentName = ""
        assertEquals(rawDiseaseAgentName, covPassValueSetsRepository.getDiseaseAgentName(rawDiseaseAgentName))
    }

    @Test
    fun `validate invalid disease agent name`() {
        val rawDiseaseAgentName = "1119349007XYZ"
        assertEquals(rawDiseaseAgentName, covPassValueSetsRepository.getDiseaseAgentName(rawDiseaseAgentName))
    }

    private fun CovPassValueSet.toCovPassValueSetLocal() = CovPassValueSetLocal(
        hash = hash,
        valueSetDate = valueSetDate,
        valueSetValues = valueSetValues,
        valueSetId = valueSetId,
    )
}
