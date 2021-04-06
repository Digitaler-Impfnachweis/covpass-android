package com.ibm.health.vaccination.sdk.android.qr.decode.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

internal object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    /**
     * Supported patterns are "yyyyMMdd" & "yyyy-MM-dd"
     */
    private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
        .toFormatter()

    override fun deserialize(decoder: Decoder): LocalDate {
        val string = decoder.decodeString()
        return LocalDate.parse(string, formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        val string = value.format(formatter)
        encoder.encodeString(string)
    }
}
