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

public fun LocalDate?.formatDateOrEmpty(): String {
    return this?.formatDate() ?: ""
}

public fun LocalDate?.isOlderThanTwoWeeks(): Boolean {
    return this?.plusDays(14)?.isBefore(LocalDate.now()) ?: false
}
