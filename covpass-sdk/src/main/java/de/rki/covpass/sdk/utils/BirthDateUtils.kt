/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.BirthDate
import de.rki.covpass.sdk.cert.models.BirthDate.Companion.BIRTH_DATE_EMPTY
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

/**
 * Formats the birth date to e.g. "1989-03-12", "1989-03-XX", "1989-XX-XX", ""
 * according to the provided format (LocalDate, YearMonth, Year)
 */
public fun BirthDate.formatInternationalOrEmpty(): String {
    return when (birthDate) {
        is LocalDate -> {
            if (birthDate == BIRTH_DATE_EMPTY) {
                ""
            } else {
                birthDate.toString()
            }
        }
        is YearMonth -> "$birthDate-XX"
        is Year -> "$birthDate-XX-XX"
        else -> ""
    }
}

/**
 * Formats the birth date to e.g. "12.03.1989", "XX.03.1989", "XX.XX.1989", ""
 * according to the provided format (LocalDate, YearMonth, Year)
 */
public fun BirthDate.formatOrEmpty(): String {
    return when (birthDate) {
        is LocalDate -> {
            if (birthDate == BIRTH_DATE_EMPTY) {
                ""
            } else {
                birthDate.formatDateOrEmpty()
            }
        }
        is YearMonth -> "XX.${birthDate.month}.${birthDate.year}"
        is Year -> "XX.XX.$birthDate"
        else -> ""
    }
}
