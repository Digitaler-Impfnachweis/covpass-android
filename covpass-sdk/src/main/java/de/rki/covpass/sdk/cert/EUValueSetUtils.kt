/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.storage.EUValueSetRepository

/**
 * Retrieves the Manufacturers name as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "ORG-100030215")
 *
 * @return The matching EU value set (e.g. "Biontech Manufacturing GmbH")
 */
public fun getManufacturerName(rawName: String): String {
    return EUValueSetRepository.vaccineManufacturer.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the Product name as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "EU/1/20/1528")
 *
 * @return The matching EU value set (e.g. "Comirnaty")
 */
public fun getProductName(rawName: String): String {
    return EUValueSetRepository.vaccineMedicalProduct.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the Prophylaxis name as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "1119349007")
 *
 * @return The matching EU value set (e.g. "SARS-CoV-2 mRNA vaccine")
 */
public fun getProphylaxisName(rawName: String): String {
    return EUValueSetRepository.vaccineProphylaxis.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the test type as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "LP6464-4")
 *
 * @return The matching EU value set (e.g. "Nucleic acid amplification with probe detection")
 */
public fun getTestTypeName(rawName: String): String {
    return EUValueSetRepository.testType.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the test result as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "260415000")
 *
 * @return The matching EU value set (e.g. "Not detected")
 */
public fun getTestResultName(rawName: String): String {
    return EUValueSetRepository.testResult.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the test manufacturer as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "1232")
 *
 * @return The matching EU value set (e.g. "Abbott Rapid Diagnostics, Panbio COVID-19 Ag Test")
 */
public fun getTestManufacturerName(rawName: String): String {
    return EUValueSetRepository.testManufacturer.valueSetValues[rawName]?.display ?: rawName
}

/**
 * Retrieves the disease agent as defined in EU value set
 *
 * @param rawName The raw consent of the EU JSON Schema (e.g. "LP6464-4")
 *
 * @return The matching EU value set (e.g. "COVID-19")
 */
public fun getDiseaseAgentName(rawName: String): String {
    return EUValueSetRepository.diseaseAgent.valueSetValues[rawName]?.display ?: rawName
}
