/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import com.upokecenter.cbor.CBORObject
import java.time.Instant

/**
 * Data model for a cbor web token.
 */
public data class CBORWebToken(
    val issuer: String = "",
    val validFrom: Instant? = null,
    val validUntil: Instant,
    val rawCbor: CBORObject,
) {
    public companion object {

        /**
         * Decodes the given [ByteArray] to [CBORWebToken].
         */
        public fun decode(data: ByteArray): CBORWebToken {
            val cbor = CBORObject.DecodeFromBytes(data)
            return CBORWebToken(
                issuer = cbor[1].AsString(),
                // Some countries use a strange timestamp format like "1624550210.984", so we have to parse first to
                // CBORNumber and then to Int.
                validFrom = cbor[6]?.AsNumber()?.ToInt64Checked()?.let { Instant.ofEpochSecond(it) },
                validUntil = Instant.ofEpochSecond(cbor[4].AsNumber().ToInt64Checked()),
                rawCbor = cbor
            )
        }
    }
}
