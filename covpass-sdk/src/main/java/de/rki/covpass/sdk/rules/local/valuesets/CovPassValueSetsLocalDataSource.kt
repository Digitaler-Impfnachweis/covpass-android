/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.valuesets

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.CovPassValueSet
import de.rki.covpass.sdk.rules.local.toCovPassValueSetsLocal
import kotlinx.coroutines.invoke

@Suppress("SpreadOperator")
public class CovPassValueSetsLocalDataSource(
    private val covPassValueSetsDao: CovPassValueSetsDao
) {

    public suspend fun update(
        keep: Collection<String>,
        add: List<CovPassValueSet>
    ) {
        dispatchers.io {
            covPassValueSetsDao.replace(
                keep = keep,
                add = add.toCovPassValueSetsLocal()
            )
        }
    }

    public suspend fun getAll(): List<CovPassValueSetLocal> =
        dispatchers.io {
            covPassValueSetsDao.getAll()
        }
}
