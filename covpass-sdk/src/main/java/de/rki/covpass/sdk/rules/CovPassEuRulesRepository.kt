/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRuleLocal
import de.rki.covpass.sdk.rules.local.rules.eu.CovPassEuRulesLocalDataSource
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public class CovPassEuRulesRepository(
    private val localDataSourceEu: CovPassEuRulesLocalDataSource,
) : CovPassRulesRepository {

    public suspend fun getAllRules(): List<CovPassEuRuleLocal> {
        return localDataSourceEu.getAllRules()
    }

    public suspend fun prepopulate(rules: List<CovPassRule>) {
        localDataSourceEu.replaceRules(keep = emptyList(), add = rules)
    }

    public override suspend fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
    ): List<CovPassRule> = localDataSourceEu.getRulesBy(
        countryIsoCode,
        validationClock,
        type,
        ruleCertificateType,
    )

    public suspend fun getRulesByType(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
    ): List<CovPassRule> = localDataSourceEu.getRulesByType(
        countryIsoCode,
        validationClock,
        type,
    )

    public suspend fun deleteAll() {
        localDataSourceEu.deleteAll()
    }
}
