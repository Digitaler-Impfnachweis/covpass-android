/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassRulesRemoteDataSource
import de.rki.covpass.sdk.rules.local.rules.CovPassRuleLocal
import de.rki.covpass.sdk.rules.local.rules.CovPassRulesLocalDataSource
import de.rki.covpass.sdk.rules.remote.rules.toCovPassRule
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public class CovPassRulesRepository(
    private val remoteDataSource: CovPassRulesRemoteDataSource,
    private val localDataSource: CovPassRulesLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository
) {

    public suspend fun getAllCovPassRules(): List<CovPassRuleLocal> {
        return localDataSource.getAllCovPassRules()
    }

    public suspend fun prepopulate(rules: List<CovPassRule>) {
        localDataSource.replaceRules(keep = emptyList(), add = rules)
    }

    public suspend fun loadRules() {
        val remoteIdentifiers =
            remoteDataSource.getRuleIdentifiers().distinctGroupBy { it.identifier }

        val localRules = localDataSource.getAllCovPassRules().distinctGroupBy { it.identifier }

        val added = remoteIdentifiers - localRules.keys
        val removed = localRules - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localRules && v.hash != localRules[k]?.hash
        }

        val newRules = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getRule(
                identifier.country.lowercase(),
                identifier.hash
            ).toCovPassRule(identifier.hash)
        }

        // Do a transactional update of the DB (as far as that's possible).
        localDataSource.replaceRules(
            keep = (localRules - changed.keys - removed.keys).keys,
            add = newRules
        )
        rulesUpdateRepository.markRulesUpdated()
    }

    public suspend fun getCovPassRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<CovPassRule> = localDataSource.getCovPassRulesBy(
        countryIsoCode, validationClock, type, ruleCertificateType
    )
}
