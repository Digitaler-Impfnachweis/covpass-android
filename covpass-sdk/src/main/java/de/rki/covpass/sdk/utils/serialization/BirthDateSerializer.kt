/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import de.rki.covpass.sdk.cert.models.BirthDate
import de.rki.covpass.sdk.cert.models.BirthDate.Companion.BIRTH_DATE_EMPTY
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@Serializer(forClass = BirthDate::class)
internal object BirthDateSerializer : KSerializer<BirthDate> {

    private const val EMPTY = 0
    private const val YEAR_COUNT = 4
    private const val YEAR_MONTH_COUNT = 7
    private const val YEAR_MONTH_DAY_COUNT = 10

    override fun deserialize(decoder: Decoder): BirthDate {
        val stringToParse = decoder.decodeString()

        return when (stringToParse.count()) {
            EMPTY -> BirthDate(BIRTH_DATE_EMPTY)
            YEAR_COUNT -> BirthDate(Year.parse(stringToParse))
            YEAR_MONTH_COUNT -> BirthDate(YearMonth.parse(stringToParse))
            YEAR_MONTH_DAY_COUNT -> BirthDate(LocalDate.parse(stringToParse))
            else -> throw DateTimeException("Invalid Birth Date format $stringToParse")
        }
    }

    override fun serialize(encoder: Encoder, value: BirthDate) {
        if (value.birthDate == BIRTH_DATE_EMPTY) {
            encoder.encodeString("")
        } else {
            encoder.encodeString(value.birthDate.toString())
        }
    }
}
