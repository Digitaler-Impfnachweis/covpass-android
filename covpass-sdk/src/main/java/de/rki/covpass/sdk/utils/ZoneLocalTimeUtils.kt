package de.rki.covpass.sdk.utils

import java.time.ZonedDateTime

public fun ZonedDateTime?.isOlderThan(hours: Long): Boolean {
    return this?.plusHours(hours)?.isBefore(ZonedDateTime.now(this.zone)) ?: false
}
