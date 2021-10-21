/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import de.rki.covpass.sdk.utils.formatDateInternational
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

public class ConvertersTest {

    private val converters by lazy { Converters() }

    @Test
    public fun `test timestampToLocalDate`() {
        val timestamp: Long = 1634169600000
        val localDate = converters.timestampToLocalDate(timestamp)
        assertEquals(localDate.formatDateInternational(), "2021-10-14")
    }

    @Test
    public fun `test localDateToTimestamp`() {
        val localDate = LocalDate.parse("2021-10-14")
        val timestamp = converters.localDateToTimestamp(localDate)
        assertEquals(timestamp, 1634169600000)
    }

    @Test
    public fun `test zonedDateTimeToTimestamp`() {
        val zonedDateTime = ZonedDateTime.parse("2021-10-14T18:25:14+00:00")
        val timestamp = converters.zonedDateTimeToTimestamp(zonedDateTime)
        assertEquals(timestamp, 1634235914000)
    }
}
