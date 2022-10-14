/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.rules.domain.rules.CovPassValidationType
import de.rki.covpass.sdk.storage.CertRepository
import dgca.verifier.app.engine.Result
import java.time.LocalDate

public class GStatusAndMaskValidator(
    private val domesticRulesValidator: CovPassRulesValidator,
) {
    public suspend fun validate(certRepository: CertRepository, region: String?) {
        val groupedCertificatesList = certRepository.certs.value
        for (groupedCert in groupedCertificatesList.certificates) {
            val mergedCertificate = groupedCert.getMergedCertificate()?.covCertificate

            if (mergedCertificate == null) {
                groupedCert.gStatus = ImmunizationStatus.Invalid
                groupedCert.maskStatus = MaskStatus.Invalid
            } else {
                // Acceptance and Invalidation rules validation
                if (
                    !isValidByType(
                        mergedCertificate,
                        CovPassValidationType.RULES,
                    )
                ) {
                    groupedCert.gStatus = ImmunizationStatus.Partial
                    groupedCert.maskStatus = MaskStatus.Required
                    continue
                } else {
                    val latestVaccination = groupedCert.getLatestValidVaccination()
                    val vaccinationDgcEntry = latestVaccination?.covCertificate?.dgcEntry as? Vaccination
                    val latestRecovery = groupedCert.getLatestValidRecovery()
                    val recoveryDgcEntry = latestRecovery?.covCertificate?.dgcEntry as? Recovery
                    val gStatus = when {
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber >= 3 -> {
                            ImmunizationStatus.Full
                        }
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber == 2 &&
                            recoveryDgcEntry != null &&
                            vaccinationDgcEntry.occurrence?.isAfter(recoveryDgcEntry.firstResult) == true -> {
                            ImmunizationStatus.Full
                        }
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber == 2 &&
                            recoveryDgcEntry != null &&
                            LocalDate.now()?.isAfter(recoveryDgcEntry.firstResult?.plusDays(29)) == true -> {
                            ImmunizationStatus.Full
                        }
                        else -> ImmunizationStatus.Partial
                    }
                    certRepository.certs.update {
                        it.certificates.map { groupedCertificate ->
                            if (groupedCertificate.id == groupedCert.id) {
                                groupedCertificate.gStatus = gStatus
                            }
                        }
                    }
                }

                // MaskStatus Validation
                val maskStatus = if (
                    isValidByType(
                        mergedCertificate,
                        CovPassValidationType.MASK,
                        region,
                    )
                ) {
                    MaskStatus.NotRequired
                } else {
                    MaskStatus.Required
                }

                certRepository.certs.update {
                    it.certificates.map { groupedCertificate ->
                        if (groupedCertificate.id == groupedCert.id) {
                            groupedCertificate.maskStatus = maskStatus
                        }
                    }
                }

                // MaskStatus Validation
                groupedCert.maskStatus =
                    if (
                        isValidByType(
                            mergedCertificate,
                            CovPassValidationType.MASK,
                            region,
                        )
                    ) {
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
        region: String? = null,
    ): Boolean {
        return domesticRulesValidator.validate(
            cert = covCertificate,
            validationType = validationType,
            region = region,
        ).all { it.result == Result.PASSED }
    }
}
