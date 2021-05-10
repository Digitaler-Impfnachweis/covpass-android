package com.ibm.health.vaccination.sdk.android.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializer(forClass = Instant::class)
internal object InstantSerializer : KSerializer<Instant> {

    override fun deserialize(decoder: Decoder): Instant =
        Instant.ofEpochSecond(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSecond)
    }
}
