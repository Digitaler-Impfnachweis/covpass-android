/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Formats a local date to e.g. "12.03.1989".
 */
public fun LocalDate.formatDate(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return format(formatter)
}

/**
 * Formats a local date to e.g. "12.03.1989". Returns an empty string for null.
 */
public fun LocalDate?.formatDateOrEmpty(): String {
    return this?.formatDate() ?: ""
}

/**
 * Formats a local date to e.g. "1989-03-28".
 */
public fun LocalDate.formatDateInternational(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return format(formatter)
}

/**
 * Checks if this [LocalDate] is older than given number [days].
 *
 * IMPORTANT: The date must be literally **older**.
 * So, passing `days = 1` would check if it's older than 1 day, which means at least 2 days old.
 *
 * @return `true` if this [LocalDate] is older than given [days], else `false`.
 */
public fun LocalDate.isOlderThan(days: Long): Boolean {
    return plusDays(days).isBefore(LocalDate.now())
}

/**
 * @return `true` if this [LocalDate] is in the future, else false.
 */
public fun LocalDate?.isInFuture(): Boolean {
    if (this == null) {
        return false
    }
    return LocalDate.now().isBefore(this)
}

/**
 * @return `true` if this [LocalDate] does not exceed [validFrom] or [validUntil], else false.
 */
public fun isValid(validFrom: LocalDate?, validUntil: LocalDate?): Boolean {
    if (validFrom == null || validUntil == null) {
        return false
    }
    val now = LocalDate.now()
    return now >= validFrom && now <= validUntil
}
