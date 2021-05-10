package com.ibm.health.vaccination.sdk.android.cert.models

import com.upokecenter.cbor.CBORObject
import java.time.Instant

public data class CBORWebToken(
    val issuer: String = "",
    val validFrom: Instant? = null,
    val validUntil: Instant,
    val rawCbor: CBORObject,
) {
    public companion object {
        public fun decode(data: ByteArray): CBORWebToken {
            val cbor = CBORObject.DecodeFromBytes(data)
            return CBORWebToken(
                issuer = cbor[1].AsString(),
                validFrom = cbor[6]?.AsInt64Value()?.let { Instant.ofEpochSecond(it) },
                validUntil = Instant.ofEpochSecond(cbor[4].AsInt64Value()),
                rawCbor = cbor
            )
        }
    }
}
