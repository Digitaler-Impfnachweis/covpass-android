/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDate::class)
internal object LocalDateSerializer : KSerializer<LocalDate> {

    private const val DATE_TIME_SEPARATOR = "T"

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun deserialize(decoder: Decoder): LocalDate {
        val stringToParse = decoder.decodeString()
        // Some countries do not comply with the EU specification and use a date time instead of a date.
        // We cut off the time to be compliant with EU specification again.
        val dateString = stringToParse.substringBefore(DATE_TIME_SEPARATOR)
        return LocalDate.parse(dateString, formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }
}
