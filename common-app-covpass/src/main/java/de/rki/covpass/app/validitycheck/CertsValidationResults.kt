/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import de.rki.covpass.sdk.cert.models.CovCertificate
import dgca.verifier.app.engine.ValidationResult

public data class CertsValidationResults(
    val cert: CovCertificate,
    val results: List<ValidationResult>
)
