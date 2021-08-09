/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.domain.rules

import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.domain.rules.GetRulesUseCase
import java.time.ZonedDateTime

public interface CovPassRulesUseCase : GetRulesUseCase {
    public suspend fun covPassInvoke(
        acceptanceCountryIsoCode: String,
        issuanceCountryIsoCode: String,
        certificateType: CertificateType,
        region: String? = null,
        validationClock: ZonedDateTime = ZonedDateTime.now(),
    ): List<Rule>
}
