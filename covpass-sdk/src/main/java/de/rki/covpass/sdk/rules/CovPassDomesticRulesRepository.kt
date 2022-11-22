/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.cert.CovPassDomesticRulesRemoteDataSource
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRuleLocal
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRulesLocalDataSource
import de.rki.covpass.sdk.rules.remote.rules.eu.toCovPassRule
import de.rki.covpass.sdk.storage.RulesUpdateRepository
import de.rki.covpass.sdk.utils.distinctGroupBy
import de.rki.covpass.sdk.utils.parallelMapNotNull
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public class CovPassDomesticRulesRepository(
    private val remoteDataSource: CovPassDomesticRulesRemoteDataSource,
    private val localDataSource: CovPassDomesticRulesLocalDataSource,
    private val rulesUpdateRepository: RulesUpdateRepository,
) : CovPassRulesRepository {

    public suspend fun getAllRules(): List<CovPassDomesticRuleLocal> {
        return localDataSource.getAllRules()
    }

    public suspend fun prepopulate(rules: List<CovPassRule>) {
        localDataSource.replaceRules(keep = emptyList(), add = rules)
    }

    public suspend fun loadRules() {
        val remoteIdentifiers =
            remoteDataSource.getRuleIdentifiers().distinctGroupBy { it.identifier }

        val localRules = localDataSource.getAllRules().distinctGroupBy { it.identifier }

        val added = remoteIdentifiers - localRules.keys
        val removed = localRules - remoteIdentifiers.keys
        val changed = remoteIdentifiers.filter { (k, v) ->
            k in localRules && v.hash != localRules[k]?.hash
        }

        val newRules = (added + changed).values.parallelMapNotNull { identifier ->
            remoteDataSource.getRule(identifier.hash).toCovPassRule(identifier.hash)
        }

        // Do a transactional update of the DB (as far as that's possible).
        localDataSource.replaceRules(
            keep = (localRules - changed.keys - removed.keys).keys,
            add = newRules,
        )
        rulesUpdateRepository.markDomesticRulesUpdated()
    }

    public override suspend fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
    ): List<CovPassRule> = localDataSource.getRulesBy(
        countryIsoCode,
        validationClock,
        type,
        ruleCertificateType,
    )

    public suspend fun getMaskRules(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
    ): List<CovPassRule> = localDataSource.getMaskRules(
        countryIsoCode,
        validationClock,
    )

    public suspend fun deleteAll() {
        localDataSource.deleteAll()
    }
}
