/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.RuleIdentifier
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import dgca.verifier.app.engine.data.source.local.rules.DefaultRulesLocalDataSource
import dgca.verifier.app.engine.data.source.local.rules.RulesDao
import dgca.verifier.app.engine.data.source.local.rules.toRules
import dgca.verifier.app.engine.data.source.local.rules.toRulesWithDescriptionLocal
import dgca.verifier.app.engine.data.source.rules.RulesDataSource
import kotlinx.coroutines.invoke
import java.time.ZonedDateTime

public class DefaultCovPassRulesLocalDataSource(
    private val rulesDao: RulesDao,
    private val ruleIdentifiersDao: RuleIdentifiersDao,
) : CovPassRulesLocalDataSource, RulesDataSource by DefaultRulesLocalDataSource(rulesDao) {

    override suspend fun replaceRules(keep: Collection<String>, add: Map<RuleIdentifier, Rule>) {
        ruleIdentifiersDao.updateRuleIdentifiers(
            keep = keep,
            add = add.keys.toRuleIdentifiersLocal()
        )
        dispatchers.io {
            rulesDao.deleteAllExcept(*keep.toTypedArray())
            rulesDao.insertAll(*add.values.toList().toRulesWithDescriptionLocal().toTypedArray())
        }
    }

    override suspend fun getAllRuleIdentifiers(): List<RuleIdentifier> {
        // Only return rule identifiers that also have a corresponding rule (i.e. where the databases are consistent).
        // This also has the nice side-effect that the next replaceRules call won't keep broken data.
        return dispatchers.io {
            val identifiers = rulesDao.getAll().map { it.identifier }.toSet()
            ruleIdentifiersDao.getAllRuleIdentifiers().toRuleIdentifiers().filter { it.identifier in identifiers }
        }
    }

    override suspend fun getCovPassRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<Rule> = dispatchers.io {
        rulesDao.getRulesWithDescriptionsBy(
            countryIsoCode,
            validationClock,
            type,
            ruleCertificateType,
            RuleCertificateType.GENERAL
        ).toRules()
    }

    override suspend fun getAllCountryCodes(): List<String> {
        return dispatchers.io {
            ruleIdentifiersDao.getAllCountryCodes()
        }
    }
}
