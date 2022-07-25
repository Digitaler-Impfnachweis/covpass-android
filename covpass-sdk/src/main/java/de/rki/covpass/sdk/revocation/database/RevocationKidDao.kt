/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.*

@Suppress("SpreadOperator")
@Dao
public abstract class RevocationKidDao {

    @Query("SELECT * FROM revocation_kid_list")
    public abstract suspend fun getAll(): List<RevocationKidLocal>

    @Query("SELECT * FROM revocation_kid_list WHERE :kid = kid")
    public abstract suspend fun getKid(kid: ByteArray): RevocationKidLocal

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertKid(rule: RevocationKidLocal): Long

    @Transaction
    public open suspend fun insertAll(list: List<RevocationKidLocal>) {
        list.forEach {
            insertKid(it)
        }
    }

    @Transaction
    public open suspend fun replaceAll(add: List<RevocationKidLocal>) {
        deleteAll()
        insertAll(add)
    }

    @Query("DELETE FROM revocation_kid_list")
    public abstract suspend fun deleteAll()
}
