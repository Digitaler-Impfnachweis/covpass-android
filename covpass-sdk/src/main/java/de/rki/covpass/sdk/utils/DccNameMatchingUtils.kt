/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Name

public object DccNameMatchingUtils {

    public fun isHolderSame(
        covCertificate1: CovCertificate,
        covCertificate2: CovCertificate
    ): Boolean {
        val trimmedName1 = covCertificate1.name.trimmedName
        val trimmedName2 = covCertificate2.name.trimmedName
        val formattedBirthDate1 = covCertificate1.birthDateFormatted
        val formattedBirthDate2 = covCertificate2.birthDateFormatted

        return compareHolder(
            trimmedName1,
            trimmedName2,
            formattedBirthDate1,
            formattedBirthDate2
        ) == DataComparison.Equal
    }

    public fun compareHolder(
        trimmedName1: Name?,
        trimmedName2: Name?,
        birthDate1: String?,
        birthDate2: String?
    ): DataComparison =
        when {
            trimmedName1 == null || trimmedName2 == null || birthDate1 == null || birthDate2 == null -> {
                DataComparison.HasNullData
            }
            birthDate1 == birthDate2 -> {
                val givenName1 = extractName(trimmedName1.givenNameTransliterated)
                val givenName2 = extractName(trimmedName2.givenNameTransliterated)
                val familyName1 = extractName(trimmedName1.familyNameTransliterated)
                val familyName2 = extractName(trimmedName2.familyNameTransliterated)
                val isGiveNameSame = (givenName1 intersect givenName2).isNotEmpty()
                val isFamilyNameSame = (familyName1 intersect familyName2).isNotEmpty()
                if (isGiveNameSame && isFamilyNameSame) {
                    DataComparison.Equal
                } else {
                    DataComparison.NameDifferent
                }
            }
            else -> {
                DataComparison.DateOfBirthDifferent
            }
        }

    private fun extractName(name: String?): Set<String> {
        if (name == null) return emptySet()
        val regex = "(?<=<\\+|)[^/<+]*(?=)".toRegex()
        return regex.findAll(name).filterNot {
            it.value.isEmpty()
        }.map { it.value }.toSet() subtract excludedTitles
    }

    private val excludedTitles = setOf(
        "DR",
        "PROF"
    )
}

public enum class DataComparison {
    Equal, NameDifferent, DateOfBirthDifferent, HasNullData
}
