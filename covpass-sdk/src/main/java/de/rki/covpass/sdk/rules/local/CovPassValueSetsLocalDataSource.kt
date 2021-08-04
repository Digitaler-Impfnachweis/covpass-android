/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import de.rki.covpass.sdk.rules.ValueSetIdentifier
import dgca.verifier.app.engine.data.ValueSet
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsDataSource

public interface CovPassValueSetsLocalDataSource : ValueSetsDataSource {

    public suspend fun replaceValueSets(
        keep: Collection<String> = emptyList(),
        add: Map<ValueSetIdentifier, ValueSet> = emptyMap(),
    )

    public suspend fun getAllValueSetIdentifiers(): List<ValueSetIdentifier>
}
