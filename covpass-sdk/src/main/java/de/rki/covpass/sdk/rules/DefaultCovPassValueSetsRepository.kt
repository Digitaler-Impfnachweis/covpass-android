/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.utils.ExperimentalHCertApi
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.local.valuesets.ValueSetsLocalDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetRemote
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetsRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.valuesets.toValueSets
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository

public class DefaultCovPassValueSetsRepository(
    private val remoteDataSource: ValueSetsRemoteDataSource,
    private val localDataSource: ValueSetsLocalDataSource
) : ValueSetsRepository {

    @ExperimentalHCertApi
    public suspend fun loadValueSets() {
        preLoad("valuesets")
    }

    override suspend fun preLoad(url: String) {
        val valueSetsRemote = mutableListOf<ValueSetRemote>()
        val valueSetsIdentifiersRemote = remoteDataSource.getValueSetsIdentifiers(url)

        valueSetsIdentifiersRemote.forEach {
            val valueSetRemote =
                remoteDataSource.getValueSet("$url/${it.hash}")
            if (valueSetRemote != null) {
                valueSetsRemote.add(valueSetRemote)
            }
        }

        if (valueSetsRemote.isNotEmpty()) {
            localDataSource.updateValueSets(valueSetsRemote.toValueSets())
        }
    }

    override suspend fun getValueSets(): List<ValueSet> = localDataSource.getValueSets()
}
