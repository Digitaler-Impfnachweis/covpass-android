/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Formats date to "12.03.1989" only in case the given date is
 * in XXXX-XX-XX format. Otherwise we show the unformatted birth date.
 */
public fun formatDateFromString(date: String): String {
    return try {
        LocalDate.parse(date).formatDate()
    } catch (e: DateTimeParseException) {
        date
    }
}
