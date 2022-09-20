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
                } else {
                    val latestVaccination = groupedCert.getLatestValidVaccination()
                    val vaccinationDgcEntry = latestVaccination?.covCertificate?.dgcEntry as? Vaccination
                    val latestRecovery = groupedCert.getLatestValidRecovery()
                    val recoveryDgcEntry = latestRecovery?.covCertificate?.dgcEntry as? Recovery
                    when {
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber >= 3 -> {
                            groupedCert.gStatus = ImmunizationStatus.Full
                        }
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber == 2 &&
                            recoveryDgcEntry != null &&
                            vaccinationDgcEntry.occurrence?.isAfter(recoveryDgcEntry.firstResult) == true -> {
                            groupedCert.gStatus = ImmunizationStatus.Full
                        }
                        vaccinationDgcEntry != null && vaccinationDgcEntry.doseNumber == 2 &&
                            recoveryDgcEntry != null &&
                            LocalDate.now()?.isAfter(recoveryDgcEntry.firstResult?.plusDays(29)) == true -> {
                            groupedCert.gStatus = ImmunizationStatus.Full
                        }
                        else -> groupedCert.gStatus = ImmunizationStatus.Partial
                    }
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
