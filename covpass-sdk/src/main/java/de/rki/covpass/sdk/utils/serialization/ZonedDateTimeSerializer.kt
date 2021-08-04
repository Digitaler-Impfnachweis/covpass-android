/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = ZonedDateTime::class)
internal object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {

    // We need to support different timezone formats, so we implement a custom timezone pattern here:
    // "sc": "2021-08-20T10:03:12Z"
    // "sc": "2021-08-20T12:03:12+02"
    // "sc": "2021-08-20T12:03:12+0200"
    // "sc": "2021-08-20T12:03:12+02:00"
    private val deserializeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private val serializeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX]")

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val dateString = decoder.decodeString()

        // Some countries use milliseconds etc. which is not really conform to the EU specification, like
        // "sc": "2021-08-20T12:03:12.000.000.000+02".
        // We remove them from the string before parsing it.
        val correctedDateString = dateString.replace("\\.[0-9]*".toRegex(), "")

        return ZonedDateTime.parse(correctedDateString, deserializeFormatter)
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(serializeFormatter))
    }
}
