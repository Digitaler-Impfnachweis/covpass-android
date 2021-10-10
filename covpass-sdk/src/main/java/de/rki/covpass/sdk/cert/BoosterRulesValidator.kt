/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.rules.booster.CovPassBoosterRulesRepository
import de.rki.covpass.sdk.utils.toZonedDateTimeOrDefault
import dgca.verifier.app.engine.data.ExternalParameter
import kotlinx.serialization.encodeToString
import java.time.ZonedDateTime

public class BoosterRulesValidator(
    private val boosterCertLogicEngine: BoosterCertLogicEngine,
    private val boosterRulesRepository: CovPassBoosterRulesRepository
) {

    public suspend fun validate(
        cert: CovCertificate,
        countryIsoCode: String = "de",
        validationClock: ZonedDateTime = ZonedDateTime.now()
    ): List<BoosterValidationResult> {
        val issuerCountryCode = cert.issuer.lowercase()
        val boosterRules = boosterRulesRepository.getCovPassBoosterRulesBy(
            countryIsoCode,
            validationClock
        )

        val externalParameter = ExternalParameter(
            validationClock = validationClock.toOffsetDateTime().toZonedDateTime(),
            valueSets = emptyMap(),
            countryCode = countryIsoCode,
            exp = cert.validUntil.toZonedDateTimeOrDefault(Long.MAX_VALUE),
            iat = cert.validFrom.toZonedDateTimeOrDefault(Long.MIN_VALUE),
            issuerCountryCode = issuerCountryCode,
            kid = "",
            region = "",
        )

        val certString = defaultJson.encodeToString(cert)
        return boosterCertLogicEngine.validate(
            cert.version,
            boosterRules,
            externalParameter,
            certString,
        )
    }
}
