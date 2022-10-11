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
    // Check mask rules
    val maskValidationResults = domesticRulesValidator.validate(
        cert = mergedCovCertificate,
        validationType = CovPassValidationType.MASK,
        region = region,
    )
    if (maskValidationResults.isEmpty()) {
        return CovPassCheckValidationResult.NoMaskRulesError
    }

    if (validateRevocation(covCertificate, revocationRemoteListRepository)) {
        return CovPassCheckValidationResult.TechnicalError
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
