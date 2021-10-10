/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.rules.local.CovPassRulesLocalDataSource
import de.rki.covpass.sdk.rules.remote.toRuleIdentifiers
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.data.source.remote.rules.RulesRemoteDataSource
import dgca.verifier.app.engine.data.source.remote.rules.toRule
import dgca.verifier.app.engine.data.source.rules.RulesRepository
import java.time.ZonedDateTime

public class DefaultCovPassRulesRepository(
    private val remoteDataSource: RulesRemoteDataSource,
    private val localDataSource: CovPassRulesLocalDataSource,
    private val dscRepository: DscRepository
) : RulesRepository {

    public suspend fun loadRules() {
        loadRules("rules")
    }

    public suspend fun getAllRuleIdentifiers(): List<RuleIdentifier> {
        return localDataSource.getAllRuleIdentifiers()
    }

    public suspend fun getAllCountryCodes(): List<String> = localDataSource.getAllCountryCodes()

    public suspend fun prepopulate(ruleIdentifiers: List<RuleIdentifier>, rules: List<Rule>) {
        val newRules = ruleIdentifiers.mapNotNull { identifier ->
            rules.find { it.identifier == identifier.identifier }?.let {
                identifier to it
            }
        }.toMap()

        localDataSource.replaceRules(keep = emptyList(), add = newRules)
    }

    override suspend fun loadRules(rulesUrl: String) {
        val remoteIdentifiers =
            remoteDataSource.getRuleIdentifiers(rulesUrl).toRuleIdentifiers().distinctGroupBy { it.identifier }
        val localIdentifiers = localDataSource.getAllRuleIdentifiers().distinctGroupBy { it.identifier }

        val added = remoteIdentifiers - localIdentifiers.keys
        val removed = localIdentifiers - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localIdentifiers && v.hash != localIdentifiers[k]?.hash
        }

        // IMPORTANT: First fetch all data and after that write everything. This reduces potential error cases where
        // some request fails and we're half-way through with updating the localDataSource and get into an inconsistent
        // DB state.
        val newRules = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getRule("$rulesUrl/${identifier.country.lowercase()}/${identifier.hash}")?.let {
                identifier to it.toRule()
            }
        }.toMap()

        // Do a transactional update of the DB (as far as that's possible).
        localDataSource.replaceRules(
            keep = (localIdentifiers - changed.keys - removed.keys).keys,
            add = newRules
        )
        dscRepository.rulesUpdate()
    }

    public suspend fun getCovPassRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule> = localDataSource.getCovPassRulesBy(
        countryIsoCode, validationClock, type, ruleCertificateType
    )

    override fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule> {
        return localDataSource.getRulesBy(
            countryIsoCode,
            validationClock,
            type,
            ruleCertificateType
        )
    }
}
