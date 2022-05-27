/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.cert.models.TestCert.Companion.ANTIGEN_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getBoosterAfterVaccinationAfterRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanVaccinationId
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
                    CertValidationResult.Valid, CertValidationResult.Revoked -> false
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
                        CertValidationResult.Valid,
                        CertValidationResult.Revoked -> {
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
            it.reissueState == ReissueState.Ready && it.hasSeenReissueDetailNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.reissueState == ReissueState.Ready) {
                    it.copy(hasSeenReissueDetailNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenReissueNotification: Boolean
        get() = certificates.any {
            it.reissueState == ReissueState.Ready && it.hasSeenReissueNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.reissueState == ReissueState.Ready) {
                    it.copy(hasSeenReissueNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenExpiredReissueNotification: Boolean
        get() = certificates.any {
            it.reissueState == ReissueState.Ready && it.hasSeenExpiredReissueNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.reissueState == ReissueState.Ready) {
                    it.copy(hasSeenExpiredReissueNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenRevokedNotification: Boolean
        get() = certificates.any {
            it.status == CertValidationResult.Revoked && it.hasSeenRevokedNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.status == CertValidationResult.Revoked && it.hasSeenRevokedNotification != value) {
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
            certificates.first().covCertificate.name.trimmedName,
            certificates.first().covCertificate.birthDate
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
            certificates.filter { it.covCertificate.dgcEntry is Vaccination }
                .sortedWith { cert1, cert2 ->
                    (cert1.covCertificate.dgcEntry as? Vaccination)?.totalSerialDoses?.compareTo(
                        (cert2.covCertificate.dgcEntry as? Vaccination)?.totalSerialDoses ?: 0
                    ) ?: 0
                }.sortedWith { cert1, cert2 ->
                    (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                        (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
                    ) ?: 0
                } + certificates.filter { it.covCertificate.dgcEntry is Recovery }
                .sortedWith { cert1, cert2 ->
                    (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                        (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
                    ) ?: 0
                } + certificates.filter { it.covCertificate.dgcEntry is TestCert }
                .sortedWith { cert1, cert2 ->
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
        return certificates.filter { it.covCertificate.dgcEntry is Vaccination }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
                ) ?: 0
            }.firstOrNull()
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Recovery]
     */
    public fun getLatestRecovery(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is Recovery }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
                ) ?: 0
            }.firstOrNull()
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [TestCert]
     */
    public fun getLatestTest(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is TestCert }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? TestCert)?.sampleCollection?.compareTo(
                    (cert1.covCertificate.dgcEntry as? TestCert)?.sampleCollection
                ) ?: 0
            }.firstOrNull()
    }

    public fun validateBoosterReissue() {
        val boosterAndVaccinationAndRecoveryIds =
            getBoosterAfterVaccinationAfterRecoveryIds(certificates)

        val listIds: List<String> = when {
            boosterAndVaccinationAndRecoveryIds.isNotEmpty() -> boosterAndVaccinationAndRecoveryIds
            else -> return
        }

        certificates = certificates.map {
            if (listIds.contains(it.covCertificate.dgcEntry.id)) {
                it.copy(
                    reissueState = ReissueState.Ready,
                    reissueType = ReissueType.Booster
                )
            } else {
                it
            }
        }.toMutableList()
    }

    public fun validateExpiredReissue() {
        val expiredGermanVaccinationId = getExpiredGermanVaccinationId(getLatestVaccination())
        val expiredGermanRecoveryIds = getExpiredGermanRecoveryIds(certificates)

        if (expiredGermanVaccinationId == null && expiredGermanRecoveryIds.isEmpty()) {
            return
        }

        certificates = certificates.map {
            if (expiredGermanRecoveryIds.contains(it.covCertificate.dgcEntry.id)) {
                it.copy(
                    reissueState = ReissueState.Ready,
                    reissueType = ReissueType.Recovery
                )
            } else {
                it
            }
        }.toMutableList()

        certificates = certificates.map {
            if (expiredGermanVaccinationId == it.covCertificate.dgcEntry.id) {
                it.copy(
                    reissueState = ReissueState.Ready,
                    reissueType = ReissueType.Vaccination
                )
            } else {
                it
            }
        }.toMutableList()
    }

    public fun showRevokedNotification(): Boolean =
        certificates.any { !it.hasSeenRevokedNotification && it.status == CertValidationResult.Revoked }

    public fun isBoosterReadyForReissue(): Boolean =
        certificates.any {
            it.reissueState == ReissueState.Ready && it.reissueType == ReissueType.Booster
        } &&
            !certificates.any {
                it.covCertificate.dgcEntry is Vaccination &&
                    (it.covCertificate.dgcEntry as Vaccination).doseNumber == 2 &&
                    (it.covCertificate.dgcEntry as Vaccination).totalSerialDoses == 1
            }

    public fun isExpiredReadyForReissue(): Boolean =
        certificates.any {
            it.reissueState == ReissueState.Ready &&
                (it.reissueType == ReissueType.Vaccination || it.reissueType == ReissueType.Recovery)
        }

    public fun finishedReissued(certId: String) {
        certificates = certificates.map {
            if (it.reissueState == ReissueState.Ready && it.covCertificate.dgcEntry.id == certId) {
                it.copy(reissueState = ReissueState.Completed)
            } else {
                it
            }
        }.toMutableList()
    }

    public fun getListOfVaccinationIdsReadyForReissue(): List<String> {
        val list = mutableListOf<String>()
        val latestVaccination = getLatestVaccination() ?: return emptyList()

        if (latestVaccination.reissueState == ReissueState.Ready) {
            list.add(latestVaccination.covCertificate.dgcEntry.id)
        } else {
            return emptyList()
        }

        list.addAll(getHistoricalDataForDcc(latestVaccination.covCertificate.dgcEntry.id))
        return list
    }

    public fun getHistoricalDataForDcc(id: String): List<String> {
        val covCertificate = certificates.find {
            it.covCertificate.dgcEntry.id == id
        }?.covCertificate

        return certificates.asSequence()
            .filterNot {
                it.covCertificate.dgcEntry.id == id
            }.filterNot {
                it.covCertificate.dgcEntry is TestCert
            }.filter {
                val date1 = (it.covCertificate.dgcEntry as? Vaccination)?.occurrence
                    ?: (it.covCertificate.dgcEntry as? Recovery)?.firstResult
                val date2 = (covCertificate?.dgcEntry as? Vaccination)?.occurrence
                    ?: (covCertificate?.dgcEntry as? Recovery)?.firstResult
                date1?.isBefore(date2) ?: false
            }.sortedWith { cert1, cert2 ->
                val date1 = (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
                    ?: (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
                val date2 = (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence
                    ?: (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult
                date1?.compareTo(date2) ?: 0
            }.take(5).map {
                it.covCertificate.dgcEntry.id
            }.toList()
    }

    public fun getListOfRecoveryIdsReadyForReissue(): List<String> {
        return certificates
            .filter {
                it.reissueState == ReissueState.Ready &&
                    it.covCertificate.dgcEntry is Recovery
            }
            .map { it.covCertificate.dgcEntry.id }
    }

    public fun getListOfIdsReadyForReissue(): List<String> {
        val list = mutableListOf<String>()
        list.addAll(
            certificates
                .filter {
                    it.reissueState == ReissueState.Ready &&
                        it.covCertificate.dgcEntry is Vaccination &&
                        (it.covCertificate.dgcEntry as Vaccination).doseNumber == 2
                }
                .map { it.covCertificate.dgcEntry.id }
        )

        list.addAll(
            certificates
                .filter {
                    it.reissueState == ReissueState.Ready &&
                        !list.contains(it.covCertificate.dgcEntry.id)
                }
                .map { it.covCertificate.dgcEntry.id }
        )
        return list
    }

    public fun getListOfImportantCerts(): List<String> {
        val list = mutableListOf<String>()
        val mainCertificate = getMainCertificate()
        val latestVaccination =
            if (mainCertificate.covCertificate.dgcEntry is Vaccination) {
                mainCertificate
            } else {
                getLatestVaccination()
            }
        val latestRecovery =
            if (mainCertificate.covCertificate.dgcEntry is Recovery) {
                mainCertificate
            } else {
                getLatestRecovery()
            }
        val latestTestCert =
            if (mainCertificate.covCertificate.dgcEntry is TestCert) {
                mainCertificate
            } else {
                getLatestTest()
            }

        if (latestTestCert?.status == CertValidationResult.Valid ||
            latestTestCert?.status == CertValidationResult.ExpiryPeriod
        ) {
            list.add(latestTestCert.covCertificate.dgcEntry.id)
        }
        if (latestVaccination?.status == CertValidationResult.Valid ||
            latestVaccination?.status == CertValidationResult.ExpiryPeriod
        ) {
            list.add(latestVaccination.covCertificate.dgcEntry.id)
        }
        if (latestRecovery?.status == CertValidationResult.Valid ||
            latestRecovery?.status == CertValidationResult.ExpiryPeriod
        ) {
            list.add(latestRecovery.covCertificate.dgcEntry.id)
        }
        return list
    }
}
