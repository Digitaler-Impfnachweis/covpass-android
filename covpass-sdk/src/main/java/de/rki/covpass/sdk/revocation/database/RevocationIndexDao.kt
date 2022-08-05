/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Suppress("SpreadOperator")
@Dao
public abstract class RevocationIndexDao {

    @Query("SELECT * FROM revocation_index_list")
    public abstract suspend fun getAll(): List<RevocationIndexLocal>

    @Query("SELECT * FROM revocation_index_list WHERE :kid = kid")
    public abstract suspend fun getAllIndexWithKid(kid: ByteArray): RevocationIndexLocal

    @Query("SELECT * FROM revocation_index_list WHERE :kid = kid AND :hashVariant = hashVariant")
    public abstract suspend fun getIndex(kid: ByteArray, hashVariant: Byte): RevocationIndexLocal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertIndex(index: RevocationIndexLocal): Long

    @Transaction
    public open suspend fun insertAll(list: List<RevocationIndexLocal>) {
        list.forEach {
            insertIndex(it)
        }
    }

    @Transaction
    public open suspend fun replaceAllFromKid(kid: ByteArray, add: List<RevocationIndexLocal>) {
        deleteAllFromKid(kid)
        insertAll(add)
    }

    @Query("DELETE FROM revocation_index_list")
    public abstract suspend fun deleteAll()

    @Query("DELETE FROM revocation_index_list WHERE :kid = kid")
    public abstract suspend fun deleteAllFromKid(kid: ByteArray)

    @Query("DELETE FROM revocation_index_list WHERE :kid = kid AND :hashVariant = hashVariant")
    public abstract suspend fun deleteElement(kid: ByteArray, hashVariant: Byte)
}
