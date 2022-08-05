/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassValueSetsRemoteDataSource
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.local.valuesets.toEuValueSet
import de.rki.covpass.sdk.rules.remote.valuesets.toCovPassValueSet
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import kotlinx.coroutines.runBlocking

public class CovPassValueSetsRepository(
    private val remoteDataSource: CovPassValueSetsRemoteDataSource,
    private val localDataSource: CovPassValueSetsLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository,
) {

    public var allCovpassValueSets: List<CovPassValueSetLocal> = emptyList()

    init {
        runBlocking {
            allCovpassValueSets = getAllCovPassValueSets()
        }
    }

    public fun getVaccineMedicalProduct(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "vaccines-covid-19-names" }

    public fun getVaccineManufacturer(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "vaccines-covid-19-auth-holders" }

    public fun getVaccineProphylaxis(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "sct-vaccines-covid-19" }

    public fun getTestType(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "covid-19-lab-test-type" }

    public fun getTestResult(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "covid-19-lab-result" }

    public fun getTestManufacturer(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "covid-19-lab-test-manufacturer-and-name" }

    public fun getDiseaseAgent(): CovPassValueSetLocal? =
        allCovpassValueSets.find { it.valueSetId == "disease-agent-targeted" }

    public suspend fun prepopulate(valueSets: List<CovPassValueSet>) {
        localDataSource.update(
            keep = emptyList(),
            add = valueSets,
        )
    }

    /**
     * Retrieves the Manufacturers name as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "ORG-100030215")
     *
     * @return The matching EU value set (e.g. "Biontech Manufacturing GmbH")
     */
    public fun getManufacturerName(rawName: String): String =
        getVaccineManufacturer()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the Product name as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "EU/1/20/1528")
     *
     * @return The matching EU value set (e.g. "Comirnaty")
     */
    public fun getProductName(rawName: String): String =
        getVaccineMedicalProduct()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the Prophylaxis name as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "1119349007")
     *
     * @return The matching EU value set (e.g. "SARS-CoV-2 mRNA vaccine")
     */
    public fun getProphylaxisName(rawName: String): String =
        getVaccineProphylaxis()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the test type as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "LP6464-4")
     *
     * @return The matching EU value set (e.g. "Nucleic acid amplification with probe detection")
     */
    public fun getTestTypeName(rawName: String): String =
        getTestType()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the test result as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "260415000")
     *
     * @return The matching EU value set (e.g. "Not detected")
     */
    public fun getTestResultName(rawName: String): String =
        getTestResult()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the test manufacturer as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "1232")
     *
     * @return The matching EU value set (e.g. "Abbott Rapid Diagnostics, Panbio COVID-19 Ag Test")
     */
    public fun getTestManufacturerName(rawName: String): String =
        getTestManufacturer()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    /**
     * Retrieves the disease agent as defined in EU value set
     *
     * @param rawName The raw consent of the EU JSON Schema (e.g. "LP6464-4")
     *
     * @return The matching EU value set (e.g. "COVID-19")
     */
    public fun getDiseaseAgentName(rawName: String): String =
        getDiseaseAgent()?.toEuValueSet()?.valueSetValues?.get(rawName)?.display ?: rawName

    public suspend fun loadValueSets() {
        val remoteIdentifiers =
            remoteDataSource.getValueSetIdentifiers().distinctGroupBy { it.id }

        val localValueSets = localDataSource.getAll().distinctGroupBy { it.valueSetId }

        val added = remoteIdentifiers - localValueSets.keys
        val removed = localValueSets - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localValueSets && v.hash != localValueSets[k]?.hash
        }

        val newValueSets = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getValueSet(
                identifier.hash,
            ).toCovPassValueSet(identifier.hash)
        }

        localDataSource.update(
            keep = (localValueSets - changed.keys - removed.keys).keys,
            add = newValueSets,
        )
        rulesUpdateRepository.markValueSetsUpdated()
        allCovpassValueSets = getAllCovPassValueSets()
    }

    public suspend fun getAllCovPassValueSets(): List<CovPassValueSetLocal> =
        localDataSource.getAll()

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
