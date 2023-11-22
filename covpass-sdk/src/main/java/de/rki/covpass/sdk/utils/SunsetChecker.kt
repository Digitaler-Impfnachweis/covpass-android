package de.rki.covpass.sdk.utils

import java.time.LocalDateTime
import java.time.Month

public object SunsetChecker {

    /**
     * From 01.01.2024 the CovPass-Check Apps will hide some selected functions because of the
     * planned sunset.
     * @return true if the given date is after 31.12.2023
     */
    public fun isSunset(timeNow: LocalDateTime = LocalDateTime.now()): Boolean {
        return timeNow.isAfter(
            LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0),
        )
    }
}
