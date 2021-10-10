/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.rules.local.CovPassValueSetsLocalDataSource
import de.rki.covpass.sdk.rules.local.toValueSetIdentifiersFromRemote
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.toValueSet
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository

public class DefaultCovPassValueSetsRepository(
    private val remoteDataSource: ValueSetsRemoteDataSource,
    private val localDataSource: CovPassValueSetsLocalDataSource
) : ValueSetsRepository {

    public suspend fun loadValueSets() {
        preLoad("valuesets")
    }

    public suspend fun getAllValueSetIdentifiers(): List<ValueSetIdentifier> {
        return localDataSource.getAllValueSetIdentifiers()
    }

    public suspend fun prepopulate(valueSetIdentifiers: List<ValueSetIdentifier>, valueSets: List<ValueSet>) {
        val newValueSets = valueSetIdentifiers.mapNotNull { identifier ->
            valueSets.find { it.valueSetId == identifier.id }?.let {
                identifier to it
            }
        }.toMap()

        localDataSource.replaceValueSets(keep = emptyList(), add = newValueSets)
    }

    override suspend fun preLoad(url: String) {
        val remoteValueSetsIdentifiers =
            remoteDataSource.getValueSetsIdentifiers(url).toValueSetIdentifiersFromRemote().distinctGroupBy { it.id }
        val localValueSetIdentifiers = localDataSource.getAllValueSetIdentifiers().distinctGroupBy { it.id }

        val added = remoteValueSetsIdentifiers - localValueSetIdentifiers.keys
        val removed = localValueSetIdentifiers - remoteValueSetsIdentifiers.keys
        val changed = remoteValueSetsIdentifiers.filter { (k, v) ->
            k in localValueSetIdentifiers && v.hash != localValueSetIdentifiers[k]?.hash
        }

        val newValueSets = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getValueSet("$url/${identifier.hash}")?.let {
                identifier to it.toValueSet()
            }
        }.toMap()

        localDataSource.replaceValueSets(
            keep = (localValueSetIdentifiers - changed.keys - removed.keys).keys,
            add = newValueSets
        )
    }

    override suspend fun getValueSets(): List<ValueSet> = localDataSource.getValueSets()
}
