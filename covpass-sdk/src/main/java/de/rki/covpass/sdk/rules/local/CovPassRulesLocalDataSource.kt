/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import de.rki.covpass.sdk.rules.RuleIdentifier
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.data.source.rules.RulesDataSource
import java.time.ZonedDateTime

// IMPORTANT: This API enforces **consistency** and **transactional** operations. Don't break these properties!
//
// We explicitly don't expose RulesLocalDataSource because that allows adding/deleting rules without their
// identifiers and thus allows bringing the DB into an inconsistent state.
//
// Since we use two separate DBs underneath, we can't actually enforce DB consistency. So we still have some bad design
// here with the separate getAllRuleIdentifiers and getAllRules calls.
//
// What we really want is to have a single Rule object that also stores the hash. Then we don't need the RuleIdentifier.
public interface CovPassRulesLocalDataSource : RulesDataSource {

    /**
     * Transactionally deletes all rules except for [keep] and then adds the given [Rule]s and [RuleIdentifier]s.
     *
     * Deleting all entries is equivalent to calling this method the default arguments (empty lists).
     */
    public suspend fun replaceRules(
        keep: Collection<String> = emptyList(),
        add: Map<RuleIdentifier, Rule> = emptyMap(),
    )

    public suspend fun getAllRuleIdentifiers(): List<RuleIdentifier>

    public suspend fun getAllCountryCodes(): List<String>

    public suspend fun getCovPassRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule>
}
