/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validitycheck

import de.rki.covpass.sdk.cert.CovPassRulesValidator
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.revocation.RevocationListRepository
import de.rki.covpass.sdk.revocation.validateRevocation
import dgca.verifier.app.engine.Result

public enum class CovPassCheckValidationResult {
    TechnicalError,
    ValidationError,
    Success
}

public suspend fun validate(
    covCertificate: CovCertificate,
    covPassRulesValidator: CovPassRulesValidator,
    revocationListRepository: RevocationListRepository,
    recoveryOlder90DaysValid: Boolean = false
): CovPassCheckValidationResult {
    val validationResults = covPassRulesValidator.validate(covCertificate)
    if (validationResults.isEmpty()) {
        return CovPassCheckValidationResult.TechnicalError
    }
    validationResults.forEach {
        if (it.result != Result.PASSED) {
            if (!(recoveryOlder90DaysValid && it.rule.identifier == "RR-DE-0002")) {
                return CovPassCheckValidationResult.ValidationError
            }
        }
    }
    if (validateRevocation(covCertificate, revocationListRepository)) {
        return CovPassCheckValidationResult.TechnicalError
    }
    return CovPassCheckValidationResult.Success
}
