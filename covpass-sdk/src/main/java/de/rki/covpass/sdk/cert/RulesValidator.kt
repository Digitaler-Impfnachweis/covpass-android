/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.rules.domain.rules.CovPassRulesUseCase
import de.rki.covpass.sdk.utils.toZonedDateTimeOrDefault
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import kotlinx.serialization.encodeToString
import java.time.ZonedDateTime

public class RulesValidator(
    private val rulesUseCase: CovPassRulesUseCase,
    private val certLogicEngine: CertLogicEngine,
    private val valueSetsRepository: ValueSetsRepository
) {

    public suspend fun validate(
        cert: CovCertificate,
        countryIsoCode: String = "de",
        validationClock: ZonedDateTime = ZonedDateTime.now()
    ): List<ValidationResult> {
        val certificateType = cert.getCertificateType()
        val issuerCountryCode = cert.issuer.lowercase()
        val rules = rulesUseCase.covPassInvoke(
            countryIsoCode,
            issuerCountryCode,
            certificateType,
            validationClock = validationClock,
        )
        val valueSetsMap = valueSetsRepository.getValueSets().map { valueSet ->
            valueSet.valueSetId to valueSet.valueSetValues.fieldNames().asSequence().toList()
        }.toMap()
        val externalParameter = ExternalParameter(
            validationClock = validationClock.toOffsetDateTime().toZonedDateTime(),
            valueSets = valueSetsMap,
            countryCode = countryIsoCode,
            exp = cert.validUntil.toZonedDateTimeOrDefault(Long.MAX_VALUE),
            iat = cert.validFrom.toZonedDateTimeOrDefault(Long.MIN_VALUE),
            issuerCountryCode = issuerCountryCode,
            kid = "",
            region = "",
        )

        val certString = defaultJson.encodeToString(cert)
        return certLogicEngine.validate(
            certificateType,
            cert.version,
            rules,
            externalParameter,
            certString,
        )
    }

    private fun CovCertificate.getCertificateType(): CertificateType =
        when (dgcEntry) {
            is Vaccination -> CertificateType.VACCINATION
            is TestCert -> CertificateType.TEST
            is Recovery -> CertificateType.RECOVERY
        }
}
