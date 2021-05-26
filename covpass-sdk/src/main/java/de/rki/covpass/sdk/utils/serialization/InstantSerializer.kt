/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializer(forClass = Instant::class)
public object InstantSerializer : KSerializer<Instant> {

    override fun deserialize(decoder: Decoder): Instant =
        Instant.ofEpochSecond(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSecond)
    }
}
