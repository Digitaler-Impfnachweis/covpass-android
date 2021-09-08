package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.utils.serialization.ZonedDateTimeSerializer
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encoding.Decoder
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ZonedDateTimeTest {

    private val mockDecoder: Decoder = mockk()
    private val testInstant by lazy { Instant.parse("2021-08-20T10:03:12Z") }

    @Test
    fun `test ZonedDateTimeSerializer valid format with full timezone`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T12:03:12+02:00"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format timezone with hours only`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T12:03:12+02"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format full timezone without colon`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T12:03:12+0200"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format timezone Z`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T10:03:12Z"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format timezone Z with milliseconds`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T12:03:12.354+0200"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format full timezone without colon with microseconds`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T06:03:12.000.000-0400"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer valid format full timezone without colon with nanoseconds`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T06:03:12.354.000.345-0400"
        val zonedDateTime = ZonedDateTimeSerializer.deserialize(mockDecoder)
        assertEquals(testInstant, zonedDateTime.toInstant())
    }

    @Test
    fun `test ZonedDateTimeSerializer invalid format without timezone`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T12:03:12"
        assertFailsWith(DateTimeParseException::class) { ZonedDateTimeSerializer.deserialize(mockDecoder) }
    }

    @Test
    fun `test ZonedDateTimeSerializer invalid format without time`() {
        every { mockDecoder.decodeString() } returns "2021-08-20T+02"
        assertFailsWith(DateTimeParseException::class) { ZonedDateTimeSerializer.deserialize(mockDecoder) }
    }

    @Test
    fun `test ZonedDateTimeSerializer invalid format without T`() {
        every { mockDecoder.decodeString() } returns "2021-08-20 12:03:12Z"
        assertFailsWith(DateTimeParseException::class) { ZonedDateTimeSerializer.deserialize(mockDecoder) }
    }
}
