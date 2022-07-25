/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revocation_kid_list")
public data class RevocationKidLocal(
    @PrimaryKey
    val kid: ByteArray,
    val hashVariants: Map<Byte, Int>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RevocationKidLocal

        if (!kid.contentEquals(other.kid)) return false
        if (hashVariants != other.hashVariants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kid.contentHashCode()
        result = 31 * result + hashVariants.hashCode()
        return result
    }
}
