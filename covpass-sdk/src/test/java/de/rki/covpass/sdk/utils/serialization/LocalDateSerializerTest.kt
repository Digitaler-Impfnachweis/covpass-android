/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.encoding.Decoder
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LocalDateSerializerTest {

    val decoder: Decoder = mockk()

    @Test
    fun `Deserialize valid date string`() {
        every { decoder.decodeString() } returns "2021-03-15"
        val date = LocalDateSerializer.deserialize(decoder)
        val expectedDate = LocalDate.of(2021, 3, 15)

        assertEquals(expectedDate, date)
    }

    @Test
    fun `Deserialize datetime string to date`() {
        every { decoder.decodeString() } returns "2021-08-20T12:03:12+02"
        val date = LocalDateSerializer.deserialize(decoder)
        val expectedDate = LocalDate.of(2021, 8, 20)

        assertEquals(expectedDate, date)
    }

    @Test(expected = DateTimeParseException::class)
    fun `Deserialize invalid string`() {
        every { decoder.decodeString() } returns "abc123"
        LocalDateSerializer.deserialize(decoder)
    }
}
