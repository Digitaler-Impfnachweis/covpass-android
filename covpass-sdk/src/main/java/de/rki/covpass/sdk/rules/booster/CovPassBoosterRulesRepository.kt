/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.booster

import de.rki.covpass.sdk.cert.BoosterRulesRemoteDataSource
import de.rki.covpass.sdk.rules.booster.local.BoosterRuleLocal
import de.rki.covpass.sdk.rules.booster.local.CovPassBoosterRulesLocalDataSource
import de.rki.covpass.sdk.rules.booster.remote.toBoosterRule
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import java.time.ZonedDateTime

public class CovPassBoosterRulesRepository(
    private val remoteDataSource: BoosterRulesRemoteDataSource,
    private val localDataSource: CovPassBoosterRulesLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository,
) {

    public suspend fun getAllBoosterRules(): List<BoosterRuleLocal> {
        return localDataSource.getAllBoosterRules()
    }

    public suspend fun prepopulate(rules: List<BoosterRule>) {
        localDataSource.replaceRules(keep = emptyList(), boosterRules = rules)
    }

    public suspend fun loadBoosterRules() {
        val remoteIdentifiers =
            remoteDataSource.getBoosterRuleIdentifiers().distinctGroupBy {
                it.identifier
            }
        val localRules = localDataSource.getAllBoosterRules().distinctGroupBy { it.identifier }

        val added = remoteIdentifiers - localRules.keys
        val removed = localRules - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localRules && v.hash != localRules[k]?.hash
        }

        val newRules = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getBoosterRule(identifier.hash).toBoosterRule(identifier.hash)
        }

        // Do a transactional update of the DB (as far as that's possible).
        localDataSource.replaceRules(
            keep = (localRules - changed.keys - removed.keys).keys,
            boosterRules = newRules,
        )
        rulesUpdateRepository.markBoosterRulesUpdated()
    }

    public suspend fun getCovPassBoosterRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<BoosterRule> =
        localDataSource.getAllBoosterRulesBy(countryIsoCode, validationClock)

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
