/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.revocation.database

import androidx.room.*

@Suppress("SpreadOperator")
@Dao
public abstract class RevocationByteTwoDao {

    @Query("SELECT * FROM revocation_byte_two_list")
    public abstract suspend fun getAll(): List<RevocationByteTwoLocal>

    @Query(
        "SELECT * " +
            "FROM revocation_byte_two_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND :byteOne = byteOne " +
            "AND :byteTwo = byteTwo"
    )
    public abstract suspend fun getByteTwoChunks(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte,
        byteTwo: Byte
    ): RevocationByteTwoLocal?

    @Query(
        "SELECT * " +
            "FROM revocation_byte_two_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND :byteOne = byteOne"
    )
    public abstract suspend fun getAllFromKidAndHashVariant(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte
    ): List<RevocationByteOneLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insertByteTwo(element: RevocationByteTwoLocal): Long

    @Transaction
    public open suspend fun insertAll(list: List<RevocationByteTwoLocal>) {
        list.forEach {
            insertByteTwo(it)
        }
    }

    @Transaction
    public open suspend fun replaceAllFromKidAndHashVariantAndByteOne(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte,
        add: List<RevocationByteTwoLocal>
    ) {
        deleteAllFromKidAndHashVariantAndByteOne(kid, hashVariant, byteOne)
        insertAll(add)
    }

    @Query(
        "DELETE FROM revocation_byte_two_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND :byteOne = byteOne"
    )
    public abstract suspend fun deleteAllFromKidAndHashVariantAndByteOne(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte
    )

    @Query(
        "DELETE FROM revocation_byte_two_list " +
            "WHERE :kid = kid " +
            "AND :hashVariant = hashVariant " +
            "AND :byteOne = byteOne " +
            "AND :byteTwo = byteTwo"
    )
    public abstract suspend fun deleteAllFromKidAndHashVariantAndByteOneAndByteTwo(
        kid: ByteArray,
        hashVariant: Byte,
        byteOne: Byte,
        byteTwo: Byte,
    )

    @Query("DELETE FROM revocation_byte_two_list WHERE :kid = kid AND :hashVariant = hashVariant ")
    public abstract suspend fun deleteAllFromKidAndHashVariant(kid: ByteArray, hashVariant: Byte)

    @Query("DELETE FROM revocation_byte_two_list WHERE :kid = kid ")
    public abstract suspend fun deleteAllFromKid(kid: ByteArray)
}
