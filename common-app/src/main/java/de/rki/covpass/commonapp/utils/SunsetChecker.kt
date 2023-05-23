package de.rki.covpass.commonapp.utils

import java.time.LocalDateTime
import java.time.Month

public object SunsetChecker {

    /**
     * From 01.07.2023 the CovPass-Check Apps will hide some selected functions because of the
     * planned sunset.
     * @return true if the given date is after 30.06.2023
     */
    public fun isSunset(timeNow: LocalDateTime = LocalDateTime.now()): Boolean {
        return timeNow.isAfter(
            LocalDateTime.of(2023, Month.JULY, 1, 0, 0, 0),
        )
    }
}
