/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.rules.domain.rules.CovPassRulesUseCase
import dgca.verifier.app.engine.CertLogicEngine
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.source.valuesets.ValueSetsRepository
import kotlinx.serialization.encodeToString
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

public class RulesValidator(
    private val rulesUseCase: CovPassRulesUseCase,
    private val certLogicEngine: CertLogicEngine,
    private val valueSetsRepository: ValueSetsRepository
) {

    public suspend fun validate(
        cert: CovCertificate,
        countryIsoCode: String = "de",
        validationClock: ZonedDateTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.id))
    ): List<ValidationResult> {
        val certificateType = cert.getCertificateType()
        val issuerCountryCode = cert.issuer.lowercase()
        val rules = rulesUseCase.covPassInvoke(
            countryIsoCode,
            issuerCountryCode,
            certificateType
        )
        val valueSetsMap = mutableMapOf<String, List<String>>()
        valueSetsRepository.getValueSets().forEach { valueSet ->
            val ids = mutableListOf<String>()
            valueSet.valueSetValues.fieldNames().forEach { id ->
                ids.add(id)
            }
            valueSetsMap[valueSet.valueSetId] = ids
        }
        val externalParameter = ExternalParameter(
            validationClock = validationClock,
            valueSets = valueSetsMap,
            countryCode = countryIsoCode,
            exp = cert.validUntil.toZonedDateTimeOrCustom(Long.MAX_VALUE),
            iat = cert.validFrom.toZonedDateTimeOrCustom(Long.MIN_VALUE),
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
            certString
        )
    }

    private fun Instant?.toZonedDateTimeOrCustom(epochMilli: Long): ZonedDateTime {
        return ZonedDateTime.ofInstant(
            this,
            ZoneId.of(ZoneOffset.UTC.id)
        ) ?: Instant.ofEpochMilli(epochMilli).atZone(ZoneOffset.UTC)
    }

    private fun CovCertificate.getCertificateType(): CertificateType {
        return when (dgcEntry) {
            is Vaccination -> CertificateType.VACCINATION
            is Test -> CertificateType.TEST
            is Recovery -> CertificateType.RECOVERY
        }
    }
}
