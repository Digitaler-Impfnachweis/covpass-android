package de.rki.covpass.sdk.utils

import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SunsetCheckerTest {

    @Test
    fun `Check before Sunset`() {
        val timeNow = LocalDateTime.of(2023, Month.JUNE, 30, 23, 59)

        assertFalse(SunsetChecker.isSunset(timeNow))
    }

    @Test
    fun `Check after Sunset`() {
        val timeNow = LocalDateTime.of(2023, Month.JULY, 1, 0, 1)

        assertTrue(SunsetChecker.isSunset(timeNow))
    }
}
