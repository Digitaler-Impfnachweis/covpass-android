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
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.getDescriptionLanguage
import de.rki.covpass.sdk.utils.isOlderThan
import dgca.verifier.app.engine.Result

public class GStatusAndMaskValidator(
    private val domesticRulesValidator: CovPassRulesValidator,
) {
    public suspend fun validate(certRepository: CertRepository, region: String?) {
        val groupedCertificatesList = certRepository.certs.value
        for (groupedCert in groupedCertificatesList.certificates) {
            // Acceptance and Invalidation rules validation
            val mergedCertificate = getPreFilteredMergedCertificate(groupedCert)
            val mergedCertificateForImmunityCheck =
                getMergedCertificateForImmunityCheck(groupedCert)

            val immunizationStatusWrapper =
                validateImmunityStatus(mergedCertificateForImmunityCheck)

            // MaskStatus Validation
            val maskStatusWrapper = if (mergedCertificate == null) {
                MaskStatusWrapper(maskStatus = MaskStatus.Invalid)
            } else {
                when (
                    isValidByType(
                        mergedCertificate,
                        CovPassValidationType.MASK,
                        region,
                    )
                ) {
                    ValidatorResult.Passed -> {
                        val vaccination = mergedCertificate.covCertificate.vaccination
                        val recovery = mergedCertificate.covCertificate.recovery
                        val test = mergedCertificate.covCertificate.test

                        val additionalDate = when {
                            vaccination != null &&
                                vaccination.occurrence?.isOlderThan(90) == false -> {
                                vaccination.occurrence.plusDays(90).formatDateOrEmpty()
                            }
                            recovery != null &&
                                recovery.firstResult?.isOlderThan(90) == false -> {
                                recovery.firstResult.plusDays(90).formatDateOrEmpty()
                            }
                            test != null &&
                                test.sampleCollection?.isOlderThan(25) == false -> {
                                test.sampleCollection.plusHours(25).formatDateTime()
                            }
                            else -> ""
                        }

                        MaskStatusWrapper(
                            maskStatus = MaskStatus.NotRequired,
                            additionalDate = additionalDate,
                        )
                    }
                    ValidatorResult.Failed -> {
                        val recovery = mergedCertificate.covCertificate.recovery
                        val recoveryFirstResultPlus28Days = if (
                            recovery != null &&
                            recovery.firstResult?.isOlderThan(29) == false
                        ) {
                            recovery.firstResult.plusDays(29).formatDateOrEmpty()
                        } else {
                            ""
                        }
                        MaskStatusWrapper(
                            maskStatus = MaskStatus.Required,
                            recoveryFirstResultPlus28Days,
                        )
                    }
                    ValidatorResult.NoRules -> MaskStatusWrapper(maskStatus = MaskStatus.NoRules)
                }
            }

            certRepository.certs.update {
                it.certificates.map { groupedCertificate ->
                    if (groupedCertificate.id == groupedCert.id) {
                        groupedCertificate.maskStatusWrapper = maskStatusWrapper
                        groupedCertificate.immunizationStatusWrapper = immunizationStatusWrapper
                    }
                }
            }
        }
    }

    private fun getMergedCertificateForImmunityCheck(
        groupedCert: GroupedCertificates,
    ): CombinedCovCertificate? {
        val latestVaccinations = groupedCert.getLatestValidVaccinations().take(10)
        val latestRecovery = groupedCert.getLatestValidRecovery()
        val latestTest = groupedCert.getLatestValidTest()

        val validDccList = buildList {
            addAll(latestVaccinations)
            add(latestRecovery)
            add(latestTest)
        }.mapNotNull { it }

        if (validDccList.isEmpty()) {
            return null
        }
        if (validDccList.size == 1) {
            return validDccList.first()
        }

        val listVaccinations = mutableListOf<Vaccination>()
        val listRecoveries = mutableListOf<Recovery>()
        val listTests = mutableListOf<TestCert>()

        validDccList.forEach {
            when (val dgc = it.covCertificate.dgcEntry) {
                is Recovery -> listRecoveries.add(dgc)
                is TestCert -> listTests.add(dgc)
                is Vaccination -> listVaccinations.add(dgc)
            }
        }

        return validDccList.first().copy(
            covCertificate = validDccList.first().covCertificate.copy(
                vaccinations = listVaccinations.sortedByDescending { it.occurrence },
                recoveries = listRecoveries.sortedByDescending { it.firstResult },
                tests = listTests.sortedByDescending { it.sampleCollection },
            ),
        )
    }

    private suspend fun getPreFilteredMergedCertificate(
        groupedCert: GroupedCertificates,
    ): CombinedCovCertificate? {
        val latestVaccinations = groupedCert.getLatestValidVaccinations()
        val latestRecovery = groupedCert.getLatestValidRecovery()
        val latestTest = groupedCert.getLatestValidTest()

        val latestVaccination = getLatestValidCertificate(latestVaccinations)

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

    private suspend fun validateImmunityStatus(
        combinedCovCertificate: CombinedCovCertificate?,
    ): ImmunizationStatusWrapper {
        if (combinedCovCertificate == null) return ImmunizationStatusWrapper(ImmunizationStatus.Invalid)

        val immunityStatusBTwo = domesticRulesValidator.validate(
            cert = combinedCovCertificate.covCertificate,
            validationType = CovPassValidationType.IMMUNITYSTATUSBTWO,
        )
        if (immunityStatusBTwo.all { it.result == Result.PASSED }) {
            return ImmunizationStatusWrapper(
                ImmunizationStatus.Full,
                immunityStatusBTwo.first().rule.getDescriptionFor(getDescriptionLanguage()),
            )
        }

        val immunityStatusCTwo = domesticRulesValidator.validate(
            cert = combinedCovCertificate.covCertificate,
            validationType = CovPassValidationType.IMMUNITYSTATUSCTWO,
        )
        if (immunityStatusCTwo.all { it.result == Result.PASSED }) {
            return ImmunizationStatusWrapper(
                ImmunizationStatus.Full,
                immunityStatusCTwo.first().rule.getDescriptionFor(getDescriptionLanguage()),
            )
        }

        val immunityStatusETwo = domesticRulesValidator.validate(
            cert = combinedCovCertificate.covCertificate,
            validationType = CovPassValidationType.IMMUNITYSTATUSETWO,
        )
        if (immunityStatusETwo.all { it.result == Result.PASSED }) {
            return ImmunizationStatusWrapper(
                ImmunizationStatus.Full,
                immunityStatusETwo.first().rule.getDescriptionFor(getDescriptionLanguage()),
            )
        }

        val immunityStatusEOne = domesticRulesValidator.validate(
            cert = combinedCovCertificate.covCertificate,
            validationType = CovPassValidationType.IMMUNITYSTATUSEONE,
        )
        if (immunityStatusEOne.all { it.result == Result.PASSED }) {
            val recovery = combinedCovCertificate.covCertificate.recovery
            val recoveryFirstResultPlus28Days = if (
                recovery != null &&
                recovery.firstResult?.isOlderThan(29) == false
            ) {
                recovery.firstResult.plusDays(29).formatDateOrEmpty()
            } else {
                ""
            }
            return ImmunizationStatusWrapper(
                immunizationStatus = ImmunizationStatus.Partial,
                immunizationText = immunityStatusEOne.first().rule.getDescriptionFor(getDescriptionLanguage()),
                fullImmunityBasedOnRecoveryDate = recoveryFirstResultPlus28Days,
            )
        }

        return ImmunizationStatusWrapper()
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

public data class ImmunizationStatusWrapper(
    val immunizationStatus: ImmunizationStatus = ImmunizationStatus.Partial,
    val immunizationText: String = "",
    val fullImmunityBasedOnRecoveryDate: String = "",
)

public data class MaskStatusWrapper(
    val maskStatus: MaskStatus = MaskStatus.Required,
    val additionalDate: String = "",
)

public enum class ValidatorResult {
    Passed, Failed, NoRules
}
