/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules

import dgca.verifier.app.engine.data.RuleCertificateType
import dgca.verifier.app.engine.data.Type
import java.time.ZonedDateTime

public interface CovPassRulesRepository {
    public suspend fun getRulesBy(
        countryIsoCode: String,
        validationClock: ZonedDateTime,
        type: Type,
        ruleCertificateType: RuleCertificateType,
    ): List<CovPassRule>
}
