/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import de.rki.covpass.sdk.rules.ValueSetIdentifier
import dgca.verifier.app.engine.data.source.remote.valuesets.ValueSetIdentifierRemote

public fun Iterable<ValueSetIdentifierLocal>.toValueSetIdentifiers(): List<ValueSetIdentifier> =
    map { it.toValueSetIdentifier() }

public fun ValueSetIdentifierLocal.toValueSetIdentifier(): ValueSetIdentifier =
    ValueSetIdentifier(
        id = this.id,
        hash = this.hash
    )

public fun Iterable<ValueSetIdentifier>.toValueSetIdentifiersLocal(): List<ValueSetIdentifierLocal> =
    map { it.toValueSetIdentifierLocal() }

public fun ValueSetIdentifier.toValueSetIdentifierLocal(): ValueSetIdentifierLocal =
    ValueSetIdentifierLocal(
        id = this.id,
        hash = this.hash
    )

public fun Iterable<ValueSetIdentifierRemote>.toValueSetIdentifiersFromRemote(): List<ValueSetIdentifier> =
    map { it.toValueSetIdentifier() }

public fun ValueSetIdentifierRemote.toValueSetIdentifier(): ValueSetIdentifier =
    ValueSetIdentifier(
        id = this.id,
        hash = this.hash
    )
