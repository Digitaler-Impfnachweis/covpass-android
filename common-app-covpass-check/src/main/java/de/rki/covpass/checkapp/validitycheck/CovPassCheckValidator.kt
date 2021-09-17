/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validitycheck

import de.rki.covpass.sdk.cert.DgcDecodeException
import de.rki.covpass.sdk.cert.RulesValidator
import de.rki.covpass.sdk.cert.models.CovCertificate
import dgca.verifier.app.engine.Result

public suspend fun validate(
    covCertificate: CovCertificate,
    rulesValidator: RulesValidator
) {
    val validationResults = rulesValidator.validate(covCertificate)
    if (validationResults.isEmpty()) {
        throw NoValidationRulesAvailableException()
    }
    validationResults.forEach {
        if (it.result != Result.PASSED) {
            throw ValidationRuleViolationException()
        }
    }
}

/**
 * This exception is thrown when no validation rules are available.
 */
public class NoValidationRulesAvailableException :
    DgcDecodeException("No validation rules available")

/**
 * This exception is thrown when a validation rule is violated.
 */
public class ValidationRuleViolationException :
    DgcDecodeException("Violation of validation rule")
