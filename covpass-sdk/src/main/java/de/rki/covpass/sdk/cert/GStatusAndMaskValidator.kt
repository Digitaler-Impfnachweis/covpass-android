/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
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
            // Acceptance and Invalidation rules validation
            val mergedCertificate = getPreFilteredMergedCertificate(groupedCert)

            val gStatus = if (mergedCertificate == null) {
                ImmunizationStatus.Invalid
            } else {
                val latestVaccination = mergedCertificate.covCertificate.vaccination
                val latestRecovery = mergedCertificate.covCertificate.recovery
                when {
                    latestVaccination != null && latestVaccination.doseNumber >= 3 -> {
                        ImmunizationStatus.Full
                    }
                    latestVaccination != null && latestVaccination.doseNumber == 2 &&
                        latestRecovery != null &&
                        latestVaccination.occurrence?.isAfter(latestRecovery.firstResult) == true -> {
                        ImmunizationStatus.Full
                    }
                    latestVaccination != null && latestVaccination.doseNumber == 2 &&
                        latestRecovery != null &&
                        LocalDate.now()?.isAfter(latestRecovery.firstResult?.plusDays(29)) == true -> {
                        ImmunizationStatus.Full
                    }
                    else -> ImmunizationStatus.Partial
                }
            }

            // MaskStatus Validation
            val maskStatus = if (mergedCertificate == null) {
                MaskStatus.Invalid
            } else {
                when (
                    isValidByType(
                        mergedCertificate,
                        CovPassValidationType.MASK,
                        region,
                    )
                ) {
                    ValidatorResult.Passed -> MaskStatus.NotRequired
                    ValidatorResult.Failed -> MaskStatus.Required
                    ValidatorResult.NoRules -> MaskStatus.NoRules
                }
            }

            certRepository.certs.update {
                it.certificates.map { groupedCertificate ->
                    if (groupedCertificate.id == groupedCert.id) {
                        groupedCertificate.maskStatus = maskStatus
                        groupedCertificate.gStatus = gStatus
                    }
                }
            }
        }
    }

    private suspend fun getPreFilteredMergedCertificate(
        groupedCert: GroupedCertificates,
    ): CombinedCovCertificate? {
        val latestVaccinations = groupedCert.getLatestValidVaccinations()
        val latestRecoveries = groupedCert.getLatestValidRecoveries()
        val latestTests = groupedCert.getLatestValidTests()

        val latestVaccination = getLatestValidCertificate(latestVaccinations)
        val latestRecovery = getLatestValidCertificate(latestRecoveries)
        val latestTest = getLatestValidCertificate(latestTests)

        val validDccList = listOfNotNull(
            latestVaccination,
            latestRecovery,
            latestTest,
        )

        val validVaccination = validDccList.firstOrNull {
            it.covCertificate.dgcEntry is Vaccination
        }
        val validRecovery = validDccList.firstOrNull {
            it.covCertificate.dgcEntry is Recovery
        }
        val validTest = validDccList.firstOrNull {
            it.covCertificate.dgcEntry is TestCert
        }

        return groupedCert.getMergedCertificate(
            validVaccination,
            validRecovery,
            validTest,
        )
    }

    private suspend fun getLatestValidCertificate(
        certificates: List<CombinedCovCertificate>,
    ): CombinedCovCertificate? {
        for (certificate in certificates) {
            if (isValidByType(certificate) == ValidatorResult.Passed) {
                return certificate
            }
        }
        return null
    }

    private suspend fun isValidByType(
        combinedCovCertificate: CombinedCovCertificate?,
        validationType: CovPassValidationType = CovPassValidationType.RULES,
        region: String? = null,
    ): ValidatorResult {
        if (combinedCovCertificate == null) return ValidatorResult.Failed
        val validateResult = domesticRulesValidator.validate(
            cert = combinedCovCertificate.covCertificate,
            validationType = validationType,
            region = region,
        )
        return when {
            validateResult.isEmpty() -> {
                ValidatorResult.NoRules
            }
            validateResult.all { it.result == Result.PASSED } -> {
                ValidatorResult.Passed
            }
            else -> {
                ValidatorResult.Failed
            }
        }
    }
}

public enum class ValidatorResult {
    Passed, Failed, NoRules
}
