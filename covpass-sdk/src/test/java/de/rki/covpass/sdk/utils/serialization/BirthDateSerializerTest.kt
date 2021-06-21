/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.rki.covpass.sdk.cert.models.BirthDate.Companion.BIRTH_DATE_EMPTY
import io.mockk.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.Test
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeParseException

internal class BirthDateSerializerTest {

    private val encoder: Encoder = mockk()
    private val decoder: Decoder = mockk()

    @Test
    fun `Serialize valid LocalDate`() {
        val birthDate = "2021-03-15"
        every { decoder.decodeString() } returns birthDate
        every { encoder.encodeString(any()) } just Runs

        val date = BirthDateSerializer.deserialize(decoder)
        BirthDateSerializer.serialize(encoder, date)

        verify { encoder.encodeString(birthDate) }
    }

    @Test
    fun `Serialize valid YearMonth`() {
        val birthDate = "2021-03"
        every { decoder.decodeString() } returns birthDate
        every { encoder.encodeString(any()) } just Runs

        val date = BirthDateSerializer.deserialize(decoder)
        BirthDateSerializer.serialize(encoder, date)

        verify { encoder.encodeString(birthDate) }
    }

    @Test
    fun `Serialize valid Year`() {
        val birthDate = "2021"
        every { decoder.decodeString() } returns birthDate
        every { encoder.encodeString(any()) } just Runs

        val date = BirthDateSerializer.deserialize(decoder)
        BirthDateSerializer.serialize(encoder, date)

        verify { encoder.encodeString(birthDate) }
    }

    @Test
    fun `Serialize empty Date`() {
        val birthDate = ""
        every { decoder.decodeString() } returns birthDate
        every { encoder.encodeString(any()) } just Runs

        val date = BirthDateSerializer.deserialize(decoder)
        BirthDateSerializer.serialize(encoder, date)

        verify { encoder.encodeString(birthDate) }
    }

    @Test
    fun `Deserialize LocalDate string`() {
        every { decoder.decodeString() } returns "2021-03-15"
        val date = BirthDateSerializer.deserialize(decoder)
        val expectedDate = LocalDate.of(2021, 3, 15)

        assertThat(date.birthDate).isEqualTo(expectedDate)
    }

    @Test
    fun `Deserialize YearMonth string`() {
        every { decoder.decodeString() } returns "2021-03"
        val date = BirthDateSerializer.deserialize(decoder)
        val expectedDate = YearMonth.of(2021, 3)

        assertThat(date.birthDate).isEqualTo(expectedDate)
    }

    @Test
    fun `Deserialize Year string`() {
        every { decoder.decodeString() } returns "2021"
        val date = BirthDateSerializer.deserialize(decoder)
        val expectedDate = Year.of(2021)

        assertThat(date.birthDate).isEqualTo(expectedDate)
    }

    @Test
    fun `Deserialize empty date`() {
        every { decoder.decodeString() } returns ""
        val date = BirthDateSerializer.deserialize(decoder)
        val expectedDate = BIRTH_DATE_EMPTY

        assertThat(date.birthDate).isEqualTo(expectedDate)
    }

    @Test(expected = DateTimeParseException::class)
    fun `Deserialize invalid LocalDate date`() {
        every { decoder.decodeString() } returns "2021-03-1X"
        BirthDateSerializer.deserialize(decoder)
    }

    @Test(expected = DateTimeParseException::class)
    fun `Deserialize invalid MonthYear date`() {
        every { decoder.decodeString() } returns "2021-1X"
        BirthDateSerializer.deserialize(decoder)
    }

    @Test(expected = DateTimeParseException::class)
    fun `Deserialize invalid Year date`() {
        every { decoder.decodeString() } returns "202X"
        BirthDateSerializer.deserialize(decoder)
    }

    @Test(expected = DateTimeException::class)
    fun `Deserialize invalid string`() {
        every { decoder.decodeString() } returns "abc123"
        BirthDateSerializer.deserialize(decoder)
    }
}
