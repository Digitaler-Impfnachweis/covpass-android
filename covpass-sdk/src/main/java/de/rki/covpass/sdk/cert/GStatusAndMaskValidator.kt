/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.rules.domain.rules.CovPassValidationType
import de.rki.covpass.sdk.storage.CertRepository
import dgca.verifier.app.engine.Result

public class GStatusAndMaskValidator(
    private val domesticRulesValidator: CovPassRulesValidator,
) {
    public suspend fun validate(certRepository: CertRepository) {
        val groupedCertificatesList = certRepository.certs.value
        for (groupedCert in groupedCertificatesList.certificates) {
            val mergedCertificate = groupedCert.getMergedCertificate()?.covCertificate

            if (mergedCertificate == null) {
                groupedCert.gStatus = ImmunizationStatus.Invalid
                groupedCert.maskStatus = MaskStatus.Invalid
            } else {
                // Acceptance and Invalidation rules validation
                if (!isValidByType(mergedCertificate, CovPassValidationType.RULES)) {
                    groupedCert.gStatus = ImmunizationStatus.Partial
                    groupedCert.maskStatus = MaskStatus.Required
                    continue
                }

                // GStatus validation
                groupedCert.gStatus = when {
                    isValidByType(mergedCertificate, CovPassValidationType.GGPLUS) ->
                        ImmunizationStatus.Full
                    isValidByType(mergedCertificate, CovPassValidationType.GG) ->
                        ImmunizationStatus.Full
                    isValidByType(mergedCertificate, CovPassValidationType.GGGPLUS) ->
                        ImmunizationStatus.Partial
                    isValidByType(mergedCertificate, CovPassValidationType.GGG) ->
                        ImmunizationStatus.Partial
                    else -> ImmunizationStatus.Partial
                }

                // MaskStatus Validation
                groupedCert.maskStatus =
                    if (isValidByType(mergedCertificate, CovPassValidationType.MASK)) {
                        MaskStatus.NotRequired
                    } else {
                        MaskStatus.Required
                    }
            }
        }
    }

    private suspend fun isValidByType(
        covCertificate: CovCertificate,
        validationType: CovPassValidationType,
    ): Boolean {
        return domesticRulesValidator.validate(
            cert = covCertificate,
            validationType = validationType,
        ).all { it.result == Result.PASSED }
    }
}
