/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote

import de.rki.covpass.sdk.rules.RuleIdentifier
import dgca.verifier.app.engine.data.source.remote.rules.RuleIdentifierRemote

public fun Iterable<RuleIdentifierRemote>.toRuleIdentifiers(): List<RuleIdentifier> =
    map { it.toRuleIdentifier() }

public fun RuleIdentifierRemote.toRuleIdentifier(): RuleIdentifier =
    RuleIdentifier(
        identifier = this.identifier,
        version = this.version,
        country = this.country,
        hash = this.hash
    )
