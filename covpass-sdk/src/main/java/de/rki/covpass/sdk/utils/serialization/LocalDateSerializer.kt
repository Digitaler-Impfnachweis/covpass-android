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

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString(), formatter)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }
}
