/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validitycheck

import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.cert.models.CovCertificate
import dgca.verifier.app.engine.Result

public enum class CovPassCheckValidationResult {
    TechnicalError,
    ValidationError,
    Success
}

public suspend fun validate(
    covCertificate: CovCertificate,
    rulesValidator: RulesValidator,
): CovPassCheckValidationResult {
    val validationResults = rulesValidator.validate(covCertificate)
    if (validationResults.isEmpty()) {
        return CovPassCheckValidationResult.TechnicalError
    }
    validationResults.forEach {
        if (it.result != Result.PASSED) {
            return CovPassCheckValidationResult.ValidationError
        }
    }
    return CovPassCheckValidationResult.Success
}
