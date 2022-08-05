/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules.domestic

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.CovPassRule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import kotlinx.coroutines.invoke
import java.time.ZonedDateTime

@Suppress("SpreadOperator")
public class CovPassDomesticRulesLocalDataSource(
    private val covPassDomesticRulesDao: CovPassDomesticRulesDao,
) {

    public suspend fun replaceRules(keep: Collection<String>, add: List<CovPassRule>) {
        dispatchers.io {
            covPassDomesticRulesDao.replaceAll(
                keep = keep,
                add = add.toCovPassDomesticRulesWithDescriptionLocal(),
            )
        }
    }

    public suspend fun getAllRules(): List<CovPassDomesticRuleLocal> =
        dispatchers.io {
            covPassDomesticRulesDao.getAll()
        }

    public suspend fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
    ): List<CovPassRule> = dispatchers.io {
        covPassDomesticRulesDao.getRulesWithDescriptionsBy(
            countryIsoCode,
            validationClock,
            type,
            ruleCertificateType,
            RuleCertificateType.GENERAL,
        ).toCovPassRules()
    }

    public suspend fun deleteAll() {
        dispatchers.io {
            covPassDomesticRulesDao.deleteAll()
        }
    }
}
