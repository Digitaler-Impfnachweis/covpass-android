/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation

import com.upokecenter.cbor.CBORObject
import de.rki.covpass.sdk.revocation.database.RevocationKidLocal

public fun validateKid(kid: ByteArray, kidList: List<RevocationKidEntry>): Boolean {
    return kidList.any { kid.contentEquals(it.kid) }
}

public fun CBORObject.toListOfByteArrays(): List<ByteArray> {
    return values.map {
        it.GetByteString()
    }
}

public fun CBORObject.toKidList(): List<RevocationKidEntry> {
    val list = mutableListOf<RevocationKidEntry>()
    entries.forEach { (key, value) ->
        list.add(
            RevocationKidEntry(
                key.GetByteString(),
                value.entries.associate { (key, value) ->
                    key.GetByteString().first() to value.AsInt32()
                }
            )
        )
    }
    return list
}

public fun CBORObject.toIndexResponse(): Map<Byte, RevocationIndexEntry> {
    return entries.associate { (key, value) ->
        key.GetByteString().first() to RevocationIndexEntry(
            value[0].AsInt64Value(),
            value[1].AsInt32(),
            value[2].entries.associate { (key2, value2) ->
                key2.GetByteString().first() to RevocationIndexByte2Entry(
                    value2[0].AsInt64Value(),
                    value2[1].AsInt32(),
                )
            },
        )
    }
}

public fun List<RevocationKidLocal>.toListOfRevocationKidEntry(): List<RevocationKidEntry> {
    return this.map {
        RevocationKidEntry(
            it.kid,
            it.hashVariants
        )
    }
}
