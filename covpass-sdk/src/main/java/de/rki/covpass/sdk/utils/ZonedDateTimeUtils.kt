/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.Duration
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

public fun ZonedDateTime.isOlderThan(hours: Long): Boolean {
    return this.plusHours(hours).isBefore(ZonedDateTime.now())
}

public fun ZonedDateTime.hoursTillNow(): Int {
    return Duration.between(this, ZonedDateTime.now()).toHours().toInt()
}

// FIXME BVC-1385 temp solution needs to be refactored after alignment
public fun ZoneOffset.getOffset1(): String {
    return when (this.toString()) {
        "Z" -> "+00:00"
        else -> this.toString()
    }
}

/**
 * Formats a [ZonedDateTime] to e.g. "1989-03-28, 14:52".
 */
public fun ZonedDateTime.formatDateTimeInternational(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, hh:mm")
    return format(formatter)
}

/**
 * Formats a [ZonedDateTime] to e.g. "12.03.1989, 14:52".
 */
public fun ZonedDateTime.formatDateTime(): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    return format(formatter)
}

/**
 * Converts a [ZonedDateTime] to the default system timezone.
 */
public fun ZonedDateTime.toDeviceTimeZone(): ZonedDateTime {
    return withZoneSameInstant(ZoneId.systemDefault())
}
