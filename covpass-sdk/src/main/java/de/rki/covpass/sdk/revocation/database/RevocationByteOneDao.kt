/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.*

@Suppress("SpreadOperator")
@Dao
public abstract class RevocationByteOneDao {

    @Query("SELECT * FROM revocation_byte_one_list")
    public abstract suspend fun getAll(): List<RevocationByteOneLocal>

    @Query(
        "SELECT * " +
            "FROM revocation_byte_one_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND :byteOne = byteOne"
    )
    public abstract suspend fun getByteOneChunks(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte
    ): RevocationByteOneLocal?

    @Query("SELECT * FROM revocation_byte_one_list WHERE :kid = kid AND :hashVariant = hashVariant")
    public abstract suspend fun getAllFromKidAndHashVariant(
        kid: ByteArray,
        hashVariant: Byte
    ): List<RevocationByteOneLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertByteOne(element: RevocationByteOneLocal): Long

    @Transaction
    public open suspend fun insertAll(list: List<RevocationByteOneLocal>) {
        list.forEach {
            insertByteOne(it)
        }
    }

    @Transaction
    public open suspend fun replaceAllFromKidAndHashVariant(
        kid: ByteArray,
        hashVariant: Byte,
        add: List<RevocationByteOneLocal>
    ) {
        deleteAllFromKidAndHashVariant(kid, hashVariant)
        insertAll(add)
    }

    @Query("DELETE FROM revocation_byte_one_list WHERE :kid = kid AND :hashVariant = hashVariant")
    public abstract suspend fun deleteAllFromKidAndHashVariant(kid: ByteArray, hashVariant: Byte)

    @Query("DELETE FROM revocation_byte_one_list WHERE :kid = kid")
    public abstract suspend fun deleteAllFromKid(kid: ByteArray)

    @Query(
        "DELETE FROM revocation_byte_one_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND byteOne == :byteOne"
    )
    public abstract suspend fun deleteElement(kid: ByteArray, hashVariant: Byte, byteOne: Byte)
}
