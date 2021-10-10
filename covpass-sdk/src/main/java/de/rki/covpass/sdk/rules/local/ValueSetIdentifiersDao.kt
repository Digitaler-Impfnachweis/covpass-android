/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Suppress("SpreadOperator")
@Dao
public abstract class ValueSetIdentifiersDao {

    @Transaction
    public open suspend fun updateValueSetsIdentifiers(
        keep: Collection<String>,
        add: Collection<ValueSetIdentifierLocal>
    ) {
        deleteAll(keep = keep)
        insertAllValueSetIdentifiers(*add.toTypedArray())
    }

    @Query("SELECT * from value_set_identifier")
    public abstract suspend fun getAllValueSetIdentifiers(): List<ValueSetIdentifierLocal>

    @Insert
    public abstract suspend fun insertAllValueSetIdentifiers(vararg valueSetIdentifierLocal: ValueSetIdentifierLocal)

    @Query("DELETE FROM value_set_identifier WHERE id NOT IN (:keep)")
    public abstract suspend fun deleteAll(keep: Collection<String> = emptyList())
}
