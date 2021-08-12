/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

public sealed interface CertValidationResult {
    public object Valid : CertValidationResult
    public object Invalid : CertValidationResult
    public object Expired : CertValidationResult
    public object ExpiryPeriod : CertValidationResult
}

public data class CombinedCovCertificate(
    val covCertificate: CovCertificate,
    val qrContent: String,
    val timestamp: Long,
    val status: CertValidationResult,
) {

    /**
     * @return the Boolean flag which indicates a positive PCR or Antigen test
     */
    public fun isPositivePcrOrAntigenTest(): Boolean {
        return (this.covCertificate.dgcEntry as? Test)?.isPositive == true
    }
}
