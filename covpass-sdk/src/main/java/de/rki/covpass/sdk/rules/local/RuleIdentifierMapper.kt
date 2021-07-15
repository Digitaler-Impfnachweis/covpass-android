/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import de.rki.covpass.sdk.rules.RuleIdentifier

public fun Iterable<RuleIdentifierLocal>.toRuleIdentifiers(): List<RuleIdentifier> =
    map { it.toRuleIdentifier() }

public fun RuleIdentifierLocal.toRuleIdentifier(): RuleIdentifier =
    RuleIdentifier(
        identifier = this.identifier,
        version = this.version,
        country = this.country,
        hash = this.hash
    )

public fun Iterable<RuleIdentifier>.toRuleIdentifiersLocal(): List<RuleIdentifierLocal> =
    map { it.toRuleIdentifierLocal() }

public fun RuleIdentifier.toRuleIdentifierLocal(): RuleIdentifierLocal =
    RuleIdentifierLocal(
        identifier = this.identifier,
        version = this.version,
        country = this.country,
        hash = this.hash
    )
