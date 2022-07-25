/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Entity

@Entity(
    tableName = "revocation_byte_one_list",
    primaryKeys = ["kid", "hashVariant", "byteOne"]
)
public data class RevocationByteOneLocal(
    val kid: ByteArray,
    val hashVariant: Byte,
    val byteOne: Byte,
    val chunks: List<ByteArray>,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RevocationByteOneLocal

        if (!kid.contentEquals(other.kid)) return false
        if (hashVariant != other.hashVariant) return false
        if (byteOne != other.byteOne) return false
        if (chunks != other.chunks) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + hashVariant
        result = 31 * result + byteOne
        result = 31 * result + chunks.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
