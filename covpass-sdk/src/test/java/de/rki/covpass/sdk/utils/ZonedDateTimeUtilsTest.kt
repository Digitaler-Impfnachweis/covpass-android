/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
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

    @Test
    public fun `test formatDateTimeInternationalWithTimezone 2021-06-17`() {
        val zonedDateTime = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00")
        assertEquals(zonedDateTime.formatDateTimeInternationalWithTimezone(), "2021-06-17T18:38:33+0700")

        val zonedDateTime2 = zonedDateTime.plusDays(2)
        assertEquals(zonedDateTime2.formatDateTimeInternationalWithTimezone(), "2021-06-19T18:38:33+0700")
    }

    @Test
    public fun `test formatDateTime 2021-06-17`() {
        val zonedDateTime = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00")
        assertEquals(zonedDateTime.formatDateTime(), "Jun 17, 2021, 6:38 PM")

        val zonedDateTime2 = zonedDateTime.plusDays(2)
        assertEquals(zonedDateTime2.formatDateTime(), "Jun 19, 2021, 6:38 PM")
    }

    @Test
    public fun `test formatDateTimeAccessibility 2021-06-17`() {
        val zonedDateTime = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00")
        assertEquals(zonedDateTime.formatDateTimeAccessibility(), "Jun 17, 2021, 6:38:00 PM")

        val zonedDateTime2 = zonedDateTime.plusDays(2)
        assertEquals(zonedDateTime2.formatDateTimeAccessibility(), "Jun 19, 2021, 6:38:00 PM")
    }

    @Test
    public fun `test formatDateDeOrEmpty 2021-06-17`() {
        val instant = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00").toInstant()
        assertEquals(instant.formatDateDeOrEmpty(), "17.06.2021")

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.formatDateDeOrEmpty(), "19.06.2021")
    }

    @Test
    public fun `test formatTimeOrEmpty 2021-06-17`() {
        val instant = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00").toInstant()
        assertEquals(instant.formatTimeOrEmpty(), "11:38 AM")

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.formatTimeOrEmpty(), "11:38 AM")
    }

    @Test
    public fun `test toISO8601orEmpty 2021-06-17`() {
        val instant = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00").toInstant()
        assertEquals(instant.toISO8601orEmpty(), "2021-06-17")

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.toISO8601orEmpty(), "2021-06-19")
    }

    @Test
    public fun `test toRFC1123OrEmpty 2021-06-17`() {
        val instant = ZonedDateTime.parse("2021-06-17T18:38:33.213+07:00").toInstant()
        assertEquals(instant.toRFC1123OrEmpty(), "Thu, 17 Jun 2021 11:38:33 GMT")

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.toRFC1123OrEmpty(), "Sat, 19 Jun 2021 11:38:33 GMT")
    }

    @Test
    public fun `test daysTillNow 2021-06-17`() {
        val instant = ZonedDateTime.now().minusDays(67).toInstant()
        assertEquals(instant.daysTillNow(), 67)

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.daysTillNow(), 65)
    }

    @Test
    public fun `test monthTillNow 2021-06-17`() {
        val instant = ZonedDateTime.now().minusDays(67).toInstant()
        assertEquals(instant.monthTillNow(), 2)

        val instant2 = instant.plus(2, ChronoUnit.DAYS)
        assertEquals(instant2.monthTillNow(), 2)
    }
}
