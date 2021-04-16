package com.ibm.health.vaccination.sdk.android.qr.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Sealed class for the sex.
 *
 */
public sealed class Sex {
    public object Male : Sex() {
        override fun toString(): String {
            return "male"
        }
    }
    public object Female : Sex() {
        override fun toString(): String {
            return "female"
        }
    }
    public object Diverse : Sex() {
        override fun toString(): String {
            return "diverse"
        }
    }
    public class Unknown(private val value: String) : Sex() {
        override fun toString(): String {
            return value
        }
    }

    public companion object {

        /**
         * Maps the raw value to the [Sex] instance.
         *
         * @param [value] the raw value.
         */
        public fun fromValue(value: String): Sex =
            when (value) {
                Male.toString() -> Male
                Female.toString() -> Female
                Diverse.toString() -> Diverse
                else -> Unknown(value)
            }
    }
}

internal object SexSerializer : KSerializer<Sex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Sex", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Sex {
        val string = decoder.decodeString()
        return Sex.fromValue(string)
    }

    override fun serialize(encoder: Encoder, value: Sex) {
        val string = value.toString()
        encoder.encodeString(string)
    }
}
