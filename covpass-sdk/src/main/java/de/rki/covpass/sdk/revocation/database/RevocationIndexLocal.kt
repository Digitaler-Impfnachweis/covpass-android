/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Entity
import de.rki.covpass.sdk.revocation.RevocationIndexEntry

@Entity(
    tableName = "revocation_index_list",
    primaryKeys = ["kid", "hashVariant"],
)
public data class RevocationIndexLocal(
    val kid: ByteArray,
    val hashVariant: Byte,
    val index: Map<Byte, RevocationIndexEntry>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RevocationIndexLocal

        if (!kid.contentEquals(other.kid)) return false
        if (hashVariant != other.hashVariant) return false
        if (index != other.index) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + hashVariant
        result = 31 * result + index.hashCode()
        return result
    }
}
