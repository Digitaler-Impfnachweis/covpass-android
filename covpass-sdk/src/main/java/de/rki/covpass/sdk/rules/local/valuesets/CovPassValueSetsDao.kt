/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.valuesets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Suppress("SpreadOperator")
@Dao
public abstract class CovPassValueSetsDao {

    @Query("SELECT * from covpass_valuesets")
    public abstract suspend fun getAll(): List<CovPassValueSetLocal>

    @Transaction
    public open suspend fun replace(
        keep: Collection<String>,
        add: Collection<CovPassValueSetLocal>
    ) {
        deleteAll(keep = keep)
        insertAll(*add.toTypedArray())
    }

    @Insert
    public abstract suspend fun insertAll(vararg covPassValueSetLocal: CovPassValueSetLocal)

    @Query("DELETE FROM covpass_valuesets WHERE valueSetId NOT IN (:keep)")
    public abstract suspend fun deleteAll(keep: Collection<String> = emptyList())
}
