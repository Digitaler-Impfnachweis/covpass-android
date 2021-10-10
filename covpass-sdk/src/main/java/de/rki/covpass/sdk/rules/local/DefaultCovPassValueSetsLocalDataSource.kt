/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.ValueSetIdentifier
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.local.valuesets.DefaultValueSetsLocalDataSource
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsDao
import dgca.verifier.app.engine.data.source.local.valuesets.toValueSets
import dgca.verifier.app.engine.data.source.local.valuesets.toValueSetsLocal
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsDataSource
import kotlinx.coroutines.invoke

@Suppress("SpreadOperator")
public class DefaultCovPassValueSetsLocalDataSource(
    private val valueSetsDao: ValueSetsDao,
    private val valueSetIdentifiersDao: ValueSetIdentifiersDao
) : CovPassValueSetsLocalDataSource, ValueSetsDataSource by DefaultValueSetsLocalDataSource(valueSetsDao) {

    override suspend fun replaceValueSets(keep: Collection<String>, add: Map<ValueSetIdentifier, ValueSet>) {
        valueSetIdentifiersDao.updateValueSetsIdentifiers(
            keep = keep,
            add = add.keys.toValueSetIdentifiersLocal()
        )
        dispatchers.io {
            val keepValueSetsLocal = valueSetsDao.getAll().filter { it.valueSetId in keep }
            valueSetsDao.deleteAll()
            valueSetsDao.insert(
                *add.values.toList().toValueSetsLocal().toTypedArray() + keepValueSetsLocal
            )
        }
    }

    override suspend fun getAllValueSetIdentifiers(): List<ValueSetIdentifier> {
        return dispatchers.io {
            val identifiers = valueSetsDao.getAll().map { it.valueSetId }.toSet()
            valueSetIdentifiersDao.getAllValueSetIdentifiers().toValueSetIdentifiers().filter { it.id in identifiers }
        }
    }

    override suspend fun getValueSets(): List<ValueSet> {
        return dispatchers.io {
            valueSetsDao.getAll().toValueSets()
        }
    }
}
