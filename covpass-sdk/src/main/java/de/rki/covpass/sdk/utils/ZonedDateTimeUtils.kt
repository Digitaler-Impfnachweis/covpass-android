/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * @return True, if the [ZonedDateTime] is older than given [hours], else false.
 */
public fun ZonedDateTime.isOlderThan(hours: Long): Boolean {
    return this.plusHours(hours).isBefore(ZonedDateTime.now())
}

/**
 * @return The duration in hours between this [ZonedDateTime] and now.
 */
public fun ZonedDateTime.hoursTillNow(): Int {
    return Duration.between(this, ZonedDateTime.now()).toHours().toInt()
}

/**
 * Formats a [ZonedDateTime] to e.g. "1989-03-28, 14:52".
 */
public fun ZonedDateTime.formatDateTimeInternational(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm")
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
