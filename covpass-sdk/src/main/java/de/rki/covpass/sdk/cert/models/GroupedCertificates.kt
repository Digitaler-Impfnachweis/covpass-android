/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.cert.models.TestCert.Companion.ANTIGEN_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getBoosterAfterVaccinationAfterRecoveryIds
import de.rki.covpass.sdk.utils.DescriptionLanguage
import de.rki.covpass.sdk.utils.getDescriptionLanguage
import de.rki.covpass.sdk.utils.isOlderThan
import kotlinx.serialization.Serializable
import java.time.LocalDate

public enum class BoosterResult {
    Passed,
    Failed
}

@Serializable
public data class BoosterNotification(
    val result: BoosterResult = BoosterResult.Failed,
    val descriptionEn: String = "",
    val descriptionDe: String = "",
    val ruleId: String = "",
) {
    public fun getLocalizedDescription(): String {
        return when (getDescriptionLanguage()) {
            DescriptionLanguage.GERMAN.languageCode -> descriptionDe
            else -> descriptionEn
        }
    }
}

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
public data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
    var boosterNotification: BoosterNotification = BoosterNotification(),
) {

    var boosterNotificationRuleIds: List<String>
        get() = getMainCertificate().boosterNotificationRuleIds
        set(value) {
            certificates = certificates.map {
                it.copy(boosterNotificationRuleIds = value)
            }.toMutableList()
        }

    var hasSeenBoosterNotification: Boolean
        get() = certificates.any { it.hasSeenBoosterNotification }
        set(value) {
            certificates = certificates.map {
                it.copy(hasSeenBoosterNotification = value)
            }.toMutableList()
        }

    var hasSeenBoosterDetailNotification: Boolean
        get() = certificates.any { it.hasSeenBoosterDetailNotification }
        set(value) {
            certificates = certificates.map {
                it.copy(hasSeenBoosterDetailNotification = value)
            }.toMutableList()
        }

    var hasSeenExpiryNotification: Boolean
        get() = getMainCertificate().let {
            when (it.covCertificate.dgcEntry) {
                is Vaccination, is Recovery -> when (it.status) {
                    CertValidationResult.Expired, CertValidationResult.ExpiryPeriod, CertValidationResult.Invalid ->
                        !it.hasSeenExpiryNotification
                    CertValidationResult.Valid -> false
                }
                is TestCert -> false
            }
        }
        set(value) {
            certificates = certificates.map {
                if (it == getMainCertificate()) {
                    when (it.status) {
                        CertValidationResult.Expired,
                        CertValidationResult.ExpiryPeriod,
                        CertValidationResult.Invalid -> {
                            it.copy(hasSeenExpiryNotification = value)
                        }
                        CertValidationResult.Valid -> {
                            it
                        }
                    }
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenReissueDetailNotification: Boolean
        get() = certificates.any {
            it.isReadyForReissue && it.hasSeenReissueDetailNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.isReadyForReissue) {
                    it.copy(hasSeenReissueDetailNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenReissueNotification: Boolean
        get() = certificates.any {
            it.isReadyForReissue && it.hasSeenReissueNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.isReadyForReissue) {
                    it.copy(hasSeenReissueNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    /**
     * The [GroupedCertificatesId] to identify this [GroupedCertificates].
     */
    val id: GroupedCertificatesId
        get() = GroupedCertificatesId(
            certificates.first().covCertificate.name.trimmedName, certificates.first().covCertificate.birthDate
        )

    /**
     * @return The primary [CombinedCovCertificate] according to the following priority:
     *
     * #1 Test certificate
     * Negative PCR Test not older then (=<)72h
     * #2 Test certificate
     * Negative quick test, not older then (=<) 48 hrs
     * #3 Vaccination certificate
     * Latest vaccination of a vaccination series (1/1, 2/2), older then (>) 14 days
     * #4 Recovery certificate
     * Recovery after SARS-Cov-2-Infection, not older then (=<) 180 Days
     * #5. Vaccination Certificate
     * Latest vaccination of a vaccination series, not older then (=<) 14 days
     * #6. Vaccination Certificate
     * Not-latest (partial immunization) of a vaccination series (1/2)
     * #7 Recovery Certificate
     * Recovery after SARS-Cov-2-Infection, older then (>) 180 Days
     * #8 Test certificate
     * Negative PCR-Test, older then (>) 72 Hrs, or negative quick test older then (>) 48 Hrs
     */
    public fun getMainCertificate(): CombinedCovCertificate {
        val certificateSortedList =
            certificates.filter { it.covCertificate.dgcEntry is Vaccination }.sortedWith { cert1, cert2 ->
                (cert1.covCertificate.dgcEntry as? Vaccination)?.totalSerialDoses?.compareTo(
                    (cert2.covCertificate.dgcEntry as? Vaccination)?.totalSerialDoses ?: 0
                ) ?: 0
            }.sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
                ) ?: 0
            } + certificates.filter { it.covCertificate.dgcEntry is Recovery }.sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
                ) ?: 0
            } + certificates.filter { it.covCertificate.dgcEntry is TestCert }.sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? TestCert)?.sampleCollection?.compareTo(
                    (cert1.covCertificate.dgcEntry as? TestCert)?.sampleCollection
                ) ?: 0
            }
        return certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is TestCert && dgcEntry.type == TestCertType.NEGATIVE_PCR_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(PCR_TEST_EXPIRY_TIME_HOURS) == false
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is TestCert && dgcEntry.type == TestCertType.NEGATIVE_ANTIGEN_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(ANTIGEN_TEST_EXPIRY_TIME_HOURS) == false
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isBooster
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_FULL_PROTECTION
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Recovery && dgcEntry.validUntil?.isBefore(LocalDate.now()) == false
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_COMPLETE
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_INCOMPLETE
        } ?: certificateSortedList.find {
            it.covCertificate.dgcEntry is Recovery
        } ?: certificateSortedList.first()
    }

    /**
     * @return The [certificates] sorted by the date of adding them. Most recently added one first.
     */
    public fun getSortedCertificates(): List<CombinedCovCertificate> =
        certificates.sortedByDescending { it.timestamp }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Vaccination]
     */
    public fun getLatestVaccination(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is Vaccination }.sortedWith { cert1, cert2 ->
            (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
            ) ?: 0
        }.firstOrNull()
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Recovery]
     */
    public fun getLatestRecovery(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is Recovery }.sortedWith { cert1, cert2 ->
            (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
            ) ?: 0
        }.firstOrNull()
    }

    public fun validateReissue() {
        val boosterAndVaccinationAndRecoveryIds =
            getBoosterAfterVaccinationAfterRecoveryIds(certificates)

        val listIds: List<String> = when {
            boosterAndVaccinationAndRecoveryIds.isNotEmpty() -> boosterAndVaccinationAndRecoveryIds
            else -> return
        }

        certificates = certificates.map {
            if (listIds.contains(it.covCertificate.dgcEntry.id)) {
                it.copy(isReadyForReissue = true)
            } else {
                it
            }
        }.toMutableList()
    }

    public fun isReadyForReissue(): Boolean =
        certificates.any { it.isReadyForReissue && !it.alreadyReissued } &&
            !certificates.any {
                it.covCertificate.dgcEntry is Vaccination &&
                    (it.covCertificate.dgcEntry as Vaccination).doseNumber == 2 &&
                    (it.covCertificate.dgcEntry as Vaccination).totalSerialDoses == 1
            }

    public fun finishedReissued() {
        certificates = certificates.map {
            if (it.isReadyForReissue && !it.alreadyReissued) {
                it.copy(alreadyReissued = true)
            } else {
                it
            }
        }.toMutableList()
    }

    public fun getListOfIdsReadyForReissue(): List<String> {
        val list = mutableListOf<String>()
        list.addAll(
            certificates
                .filter {
                    it.isReadyForReissue &&
                        !it.alreadyReissued &&
                        it.covCertificate.dgcEntry is Vaccination &&
                        (it.covCertificate.dgcEntry as Vaccination).doseNumber == 2
                }
                .map { it.covCertificate.dgcEntry.id }
        )

        list.addAll(
            certificates
                .filter {
                    it.isReadyForReissue &&
                        !it.alreadyReissued &&
                        !list.contains(it.covCertificate.dgcEntry.id)
                }
                .map { it.covCertificate.dgcEntry.id }
        )
        return list
    }
}
