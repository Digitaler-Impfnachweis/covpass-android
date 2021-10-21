/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class ZonedDateTimeUtilsTest {

    @Test
    public fun `test isOlderThan`() {
        assertTrue {
            ZonedDateTime.now().minusHours(6).isOlderThan(5)
        }
        assertFalse {
            ZonedDateTime.now().minusHours(3).isOlderThan(5)
        }
    }

    @Test
    public fun `test hoursTillNow`() {
        assertEquals(ZonedDateTime.now().minusHours(6).hoursTillNow(), 6)
        assertEquals(ZonedDateTime.now().minusHours(2).hoursTillNow(), 2)
    }

    @Test
    public fun `test formatDateTimeInternational 2021-05-15`() {
        val zonedDateTime1 = ZonedDateTime.parse("2021-05-15T15:25:33.213+02:00")
        assertEquals(zonedDateTime1.formatDateTimeInternational(), "2021-05-15, 15:25")
    }

    @Test
    public fun `test formatDateTimeInternational 2021-06-17`() {
        val zonedDateTime = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00")
        assertEquals(zonedDateTime.formatDateTimeInternational(), "2021-06-17, 18:38")

        val zonedDateTime2 = zonedDateTime.plusDays(2)
        assertEquals(zonedDateTime2.formatDateTimeInternational(), "2021-06-19, 18:38")
    }
}
