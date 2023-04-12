/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRuleLocal
import de.rki.covpass.sdk.rules.local.rules.domestic.CovPassDomesticRulesLocalDataSource
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public class CovPassDomesticRulesRepository(
    private val localDataSource: CovPassDomesticRulesLocalDataSource,
) : CovPassRulesRepository {

    public suspend fun getAllRules(): List<CovPassDomesticRuleLocal> {
        return localDataSource.getAllRules()
    }

    public suspend fun prepopulate(rules: List<CovPassRule>) {
        localDataSource.replaceRules(keep = emptyList(), add = rules)
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
