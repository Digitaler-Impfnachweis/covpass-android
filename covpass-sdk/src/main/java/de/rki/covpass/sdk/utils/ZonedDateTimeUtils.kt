/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
