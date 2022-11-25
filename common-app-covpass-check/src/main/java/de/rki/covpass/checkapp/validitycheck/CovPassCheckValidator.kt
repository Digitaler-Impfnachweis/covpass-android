/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validitycheck

import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.revocation.RevocationRemoteListRepository
import de.rki.covpass.sdk.revocation.validateRevocation
import de.rki.covpass.sdk.rules.domain.rules.CovPassValidationType
import dgca.verifier.app.engine.Result

public enum class CovPassCheckValidationResult {
    TechnicalError,
    ValidationError,
    NoMaskRulesError,
    Success
}

public suspend fun validate(
    mergedCovCertificate: CovCertificate,
    covCertificate: CovCertificate,
    domesticRulesValidator: CovPassRulesValidator,
    euRulesValidator: CovPassRulesValidator,
    revocationRemoteListRepository: RevocationRemoteListRepository,
    region: String? = null,
): CovPassCheckValidationResult {
    if (validateRevocation(covCertificate, revocationRemoteListRepository)) {
        return CovPassCheckValidationResult.TechnicalError
    }

    // Check mask rules
    val maskValidationResults = domesticRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.MASK,
        region = region,
    )
    if (maskValidationResults.isEmpty()) {
        return CovPassCheckValidationResult.NoMaskRulesError
    }

    // Acceptance and Invalidation rules from /domesticrules
    val domesticValidationResults = domesticRulesValidator.validate(mergedCovCertificate)
    if (domesticValidationResults.any { it.result == Result.FAIL }) {
        return CovPassCheckValidationResult.ValidationError
    }

    // Invalidation rules from /rules
    val euValidationResults = euRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.INVALIDATION,
    )
    if (euValidationResults.any { it.result == Result.FAIL }) {
        return CovPassCheckValidationResult.ValidationError
    }

    // Validate mask rules
    if (maskValidationResults.any { it.result == Result.FAIL }) {
        return CovPassCheckValidationResult.ValidationError
    }

    return CovPassCheckValidationResult.Success
}

public enum class CovPassCheckImmunityValidationResult {
    TechnicalError,
    ValidationError,
    Success,
}

public suspend fun validateImmunityStatus(
    mergedCovCertificate: CovCertificate,
    covCertificate: CovCertificate,
    domesticRulesValidator: CovPassRulesValidator,
    revocationRemoteListRepository: RevocationRemoteListRepository,
): CovPassCheckImmunityValidationResult {
    if (validateRevocation(covCertificate, revocationRemoteListRepository)) {
        return CovPassCheckImmunityValidationResult.TechnicalError
    }

    // Invalidation rules from /domesticrules
    val domesticValidationResults = domesticRulesValidator.validate(
        cert = covCertificate,
        validationType = CovPassValidationType.INVALIDATION,
    )
    if (domesticValidationResults.any { it.result == Result.FAIL }) {
        return CovPassCheckImmunityValidationResult.TechnicalError
    }

    // Check immunity status b2
    val immunityStatusBTwo = domesticRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.IMMUNITYSTATUSBTWO,
    )
    if (immunityStatusBTwo.all { it.result == Result.PASSED }) {
        return CovPassCheckImmunityValidationResult.Success
    }

    // Check immunity status c2
    val immunityStatusCTwo = domesticRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.IMMUNITYSTATUSCTWO,
    )
    if (immunityStatusCTwo.all { it.result == Result.PASSED }) {
        return CovPassCheckImmunityValidationResult.Success
    }

    // Check immunity status e2
    val immunityStatusETwo = domesticRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.IMMUNITYSTATUSETWO,
    )
    if (immunityStatusETwo.all { it.result == Result.PASSED }) {
        return CovPassCheckImmunityValidationResult.Success
    }

    return CovPassCheckImmunityValidationResult.ValidationError
}

public suspend fun validateEntry(
    covCertificate: CovCertificate,
    euRulesValidator: CovPassRulesValidator,
    revocationRemoteListRepository: RevocationRemoteListRepository,
): CovPassCheckImmunityValidationResult {
    if (validateRevocation(covCertificate, revocationRemoteListRepository)) {
        return CovPassCheckImmunityValidationResult.TechnicalError
    }

    // Acceptance and Invalidation rules from /eurules
    val euValidationResults = euRulesValidator.validate(covCertificate).filterNot {
        it.rule.identifier == "GR-DE-0001"
    }
    if (euValidationResults.any { it.result == Result.FAIL }) {
        return CovPassCheckImmunityValidationResult.ValidationError
    }

    return CovPassCheckImmunityValidationResult.Success
}
