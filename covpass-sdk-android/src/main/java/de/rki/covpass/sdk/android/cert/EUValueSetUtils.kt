/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.cert

import de.rki.covpass.sdk.android.storage.EUValueSetRepository
import de.rki.covpass.sdk.android.storage.countryCodesToNames

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
 * Retrieves the Country name from the given Country code
 *
 * @param countryCode The country code in ISO-3166 (e.g. "DE")
 *
 * @return The matching Country name (e.g. "Deutschland")
 */
public fun getCountryName(countryCode: String): String {
    return countryCodesToNames[countryCode] ?: countryCode
}
