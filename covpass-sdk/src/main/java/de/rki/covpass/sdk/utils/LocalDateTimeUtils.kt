/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Formats a local date to e.g. "12.03.1989, 14:52".
 */
public fun LocalDateTime.formatDateTime(): String {
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    return format(formatter)
}

public fun LocalDateTime.formatDateTimeAccessibility(): String {
    val localDateTimeWithoutSeconds = withSecond(0)
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
    return localDateTimeWithoutSeconds.format(formatter)
}
