/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.local.rules

import com.ensody.reactivestate.dispatchers
import de.rki.covpass.sdk.rules.CovPassRule
import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import kotlinx.coroutines.invoke
import java.time.ZonedDateTime

@Suppress("SpreadOperator")
public class CovPassRulesLocalDataSource(
    private val covPassRulesDao: CovPassRulesDao
) {

    public suspend fun replaceRules(keep: Collection<String>, add: List<CovPassRule>) {
        dispatchers.io {
            covPassRulesDao.replaceAll(
                keep = keep,
                add = add.toCovPassRulesWithDescriptionLocal()
            )
        }
    }

    public suspend fun getAllCovPassRules(): List<CovPassRuleLocal> =
        dispatchers.io {
            covPassRulesDao.getAll()
        }

    public suspend fun getCovPassRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType
    ): List<CovPassRule> = dispatchers.io {
        covPassRulesDao.getCovPassRulesWithDescriptionsBy(
            countryIsoCode,
            validationClock,
            type,
            ruleCertificateType,
            RuleCertificateType.GENERAL
        ).toCovPassRules()
    }

    public suspend fun deleteAll() {
        dispatchers.io {
            covPassRulesDao.deleteAll()
        }
    }
}
