/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassValueSetsRemoteDataSource
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetLocal
import de.rki.covpass.sdk.rules.local.valuesets.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.remote.valuesets.toCovPassValueSet
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull

public class CovPassValueSetsRepository(
    private val remoteDataSource: CovPassValueSetsRemoteDataSource,
    private val localDataSource: CovPassValueSetsLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository,
) {

    public suspend fun prepopulate(valueSets: List<CovPassValueSet>) {
        localDataSource.update(
            keep = emptyList(),
            add = valueSets
        )
    }

    public suspend fun loadValueSets() {
        val remoteIdentifiers =
            remoteDataSource.getValueSetIdentifiers().distinctGroupBy { it.id }

        val localValueSets = localDataSource.getAll().distinctGroupBy { it.valueSetId }

        val added = remoteIdentifiers - localValueSets.keys
        val removed = localValueSets - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localValueSets && v.hash != localValueSets[k]?.hash
        }

        val newValueSets = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getValueSet(
                identifier.hash
            ).toCovPassValueSet(identifier.hash)
        }

        localDataSource.update(
            keep = (localValueSets - changed.keys - removed.keys).keys,
            add = newValueSets
        )
        rulesUpdateRepository.markValueSetsUpdated()
    }

    public suspend fun getAllCovPassValueSets(): List<CovPassValueSetLocal> =
        localDataSource.getAll()

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
