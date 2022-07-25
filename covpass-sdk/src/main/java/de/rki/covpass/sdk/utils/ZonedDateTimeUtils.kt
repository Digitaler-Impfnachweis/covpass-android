/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.*
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
 * Formats a [ZonedDateTime] to e.g. "1989-03-28T14:52:00+0000".
 */
public fun ZonedDateTime.formatDateTimeInternationalWithTimezone(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXX")
    return format(formatter)
}

/**
 * Formats a [ZonedDateTime] to e.g. "12.03.1989, 14:52".
 */
public fun ZonedDateTime.formatDateTime(): String {
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    return format(formatter)
}

/**
 * Formats a [ZonedDateTime] to e.g. "12.03.1989, 14:52:00" for accessibility.
 */
public fun ZonedDateTime.formatDateTimeAccessibility(): String {
    val zonedDateTimeWithoutSeconds = withSecond(0)
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
    return zonedDateTimeWithoutSeconds.format(formatter)
}

/**
 * Converts a [ZonedDateTime] to the default system timezone.
 */
public fun ZonedDateTime.toDeviceTimeZone(): ZonedDateTime {
    return withZoneSameInstant(ZoneId.systemDefault())
}

public fun Instant?.formatDateOrEmpty(): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return if (this != null) {
        ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).format(formatter)
    } else {
        ""
    }
}

public fun Instant?.formatDateDeOrEmpty(): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return if (this != null) {
        ZonedDateTime.ofInstant(this, ZoneId.of("Europe/Berlin")).format(formatter)
    } else {
        ""
    }
}

public fun Instant?.formatTimeOrEmpty(): String {
    val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    return if (this != null) {
        ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).format(formatter)
    } else {
        ""
    }
}

public fun Instant?.toISO8601orEmpty(): String {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return if (this != null) {
        ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).format(formatter)
    } else {
        ""
    }
}

/**
 * Format used in "If-Modified-Since" header
 */
public fun Instant?.toRFC1123OrEmpty(): String {
    val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
    return if (this != null) {
        ZonedDateTime.ofInstant(this, ZoneId.of("UTC")).format(formatter)
    } else {
        ""
    }
}

public fun Instant?.toZonedDateTimeOrDefault(defaultEpochMilli: Long): ZonedDateTime =
    (this ?: Instant.ofEpochMilli(defaultEpochMilli)).atZone(ZoneOffset.UTC)

public fun Instant.daysTillNow(): Int {
    return Duration.between(this, Instant.now()).toDays().toInt()
}

public fun Instant.monthTillNow(): Int {
    return Duration.between(this, Instant.now()).toDays().toInt() / 30
}
