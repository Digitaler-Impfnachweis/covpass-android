/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.ensody.reactivestate.dispatchers
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsDao
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsLocalDataSource
import dgca.verifier.app.engine.data.source.local.valuesets.toValueSets
import dgca.verifier.app.engine.data.source.local.valuesets.toValueSetsLocal
import kotlinx.coroutines.invoke

public class DefaultCovPassValueSetsLocalDataSource(
    private val valueSetsDao: ValueSetsDao,
) : ValueSetsLocalDataSource {

    override suspend fun getValueSets(): List<ValueSet> {
        return dispatchers.io {
            valueSetsDao.getAll().toValueSets()
        }
    }

    override suspend fun updateValueSets(valueSets: List<ValueSet>) {
        return dispatchers.io {
            valueSetsDao.apply {
                deleteAll()
                insert(*valueSets.toValueSetsLocal().toTypedArray())
            }
        }
    }
}
