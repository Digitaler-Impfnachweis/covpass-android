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
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.rules.domain.rules.CovPassUseCase
import de.rki.covpass.sdk.rules.domain.rules.CovPassValidationType
import de.rki.covpass.sdk.rules.local.rules.eu.toRules
import de.rki.covpass.sdk.rules.local.valuesets.toValueSets
import de.rki.covpass.sdk.utils.toZonedDateTimeOrDefault
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Type
import kotlinx.serialization.encodeToString
import java.time.ZonedDateTime

public class CovPassRulesValidator(
    private val rulesUseCase: CovPassUseCase,
    private val certLogicEngine: CertLogicEngine,
    private val valueSetsRepository: CovPassValueSetsRepository,
) {

    public suspend fun validate(
        cert: CovCertificate,
        countryIsoCode: String = "de",
        validationClock: ZonedDateTime = ZonedDateTime.now(),
        validationType: CovPassValidationType = CovPassValidationType.RULES,
        region: String? = null,
    ): List<ValidationResult> {
        val certificateType = cert.getCertificateType()
        val issuerCountryCode = cert.issuer.lowercase()
        val rules = rulesUseCase.invoke(
            countryIsoCode,
            issuerCountryCode,
            certificateType,
            validationClock,
            validationType,
            region,
        )
        val valueSetsMap =
            valueSetsRepository.getAllCovPassValueSets().toValueSets().associate { valueSet ->
                valueSet.valueSetId to valueSet.valueSetValues.fieldNames().asSequence().toList()
            }
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
            rules.toRules(),
            externalParameter,
            certString,
        ).filterNot { it.rule.type == Type.INVALIDATION }
    }

    private fun CovCertificate.getCertificateType(): CertificateType =
        when (dgcEntry) {
            is Vaccination -> CertificateType.VACCINATION
            is TestCert -> CertificateType.TEST
            is Recovery -> CertificateType.RECOVERY
        }
}
