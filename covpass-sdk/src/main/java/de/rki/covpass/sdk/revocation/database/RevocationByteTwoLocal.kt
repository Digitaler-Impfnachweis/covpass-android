/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Entity

@Entity(
    tableName = "revocation_byte_two_list",
    primaryKeys = ["kid", "hashVariant", "byteOne", "byteTwo"]
)
public data class RevocationByteTwoLocal(
    val kid: ByteArray,
    val hashVariant: Byte,
    val byteOne: Byte,
    val byteTwo: Byte,
    val chunks: List<ByteArray>,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RevocationByteTwoLocal

        if (!kid.contentEquals(other.kid)) return false
        if (hashVariant != other.hashVariant) return false
        if (byteOne != other.byteOne) return false
        if (byteTwo != other.byteTwo) return false
        if (chunks != other.chunks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + hashVariant
        result = 31 * result + byteOne
        result = 31 * result + byteTwo
        result = 31 * result + chunks.hashCode()
        return result
    }
}
