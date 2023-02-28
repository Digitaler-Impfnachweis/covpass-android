/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.cert.ImmunizationStatusWrapper
import de.rki.covpass.sdk.cert.models.TestCert.Companion.ANTIGEN_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getBoosterAfterVaccinationAfterRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanAfter90DaysRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanAfter90DaysVaccinationId
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredGermanVaccinationId
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredNotGermanRecoveryIds
import de.rki.covpass.sdk.utils.CertificateReissueUtils.getExpiredNotGermanVaccinationId
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
@Suppress("LargeClass")
public data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
    var boosterNotification: BoosterNotification = BoosterNotification(),
    var immunizationStatusWrapper: ImmunizationStatusWrapper = ImmunizationStatusWrapper(),
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

    var hasSeenNotGermanReissueNotification: Boolean
        get() = certificates.any {
            it.reissueState == ReissueState.NotGermanReady && it.hasSeenReissueNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.reissueState == ReissueState.NotGermanReady) {
                    it.copy(hasSeenReissueNotification = value)
                } else {
                    it
                }
            }.toMutableList()
        }

    var hasSeenExpiredReissueNotification: Boolean
        get() = !certificates.any {
            (it.reissueState == ReissueState.Ready || it.reissueState == ReissueState.NotGermanReady) &&
                !it.hasSeenExpiredReissueNotification
        }
        set(value) {
            certificates = certificates.map {
                if (it.reissueState == ReissueState.Ready || it.reissueState == ReissueState.NotGermanReady) {
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
            certificates.first().covCertificate.birthDate,
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
                        (cert2.covCertificate.dgcEntry as? Vaccination)?.totalSerialDoses ?: 0,
                    ) ?: 0
                }.sortedWith { cert1, cert2 ->
                    (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                        (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence,
                    ) ?: 0
                } + certificates.filter { it.covCertificate.dgcEntry is Recovery }
                .sortedWith { cert1, cert2 ->
                    (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                        (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult,
                    ) ?: 0
                } + certificates.filter { it.covCertificate.dgcEntry is TestCert }
                .sortedWith { cert1, cert2 ->
                    (cert2.covCertificate.dgcEntry as? TestCert)?.sampleCollection?.compareTo(
                        (cert1.covCertificate.dgcEntry as? TestCert)?.sampleCollection,
                    ) ?: 0
                }
        return certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is TestCert && dgcEntry.type == TestCertType.NEGATIVE_PCR_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(PCR_TEST_EXPIRY_TIME_HOURS) == false &&
                !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is TestCert && dgcEntry.type == TestCertType.NEGATIVE_ANTIGEN_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(ANTIGEN_TEST_EXPIRY_TIME_HOURS) == false &&
                !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isBooster && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_FULL_PROTECTION && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Recovery && dgcEntry.validUntil?.isBefore(LocalDate.now()) == false &&
                !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_COMPLETE && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_INCOMPLETE && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            it.covCertificate.dgcEntry is Recovery && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            it.covCertificate.dgcEntry is TestCert && !it.isExpiredOrRevoked()
        } ?: certificateSortedList.find {
            it.covCertificate.isExpired()
        } ?: certificateSortedList.find {
            it.isRevoked
        } ?: certificateSortedList.first()
    }

    public fun updateReissueNotificationForCertificate(value: Boolean, certId: String) {
        certificates = certificates.map {
            if (
                (it.reissueState == ReissueState.Ready || it.reissueState == ReissueState.NotGermanReady) &&
                it.covCertificate.dgcEntry.id == certId
            ) {
                it.copy(hasSeenExpiredReissueNotification = value)
            } else {
                it
            }
        }.toMutableList()
    }

    private fun CombinedCovCertificate.isExpiredOrRevoked() =
        covCertificate.isExpired() || isRevoked || status == CertValidationResult.Invalid

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
                    (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence,
                ) ?: 0
            }.firstOrNull()
    }

    /**
     * @return The latest valid [CombinedCovCertificate] that is a [Vaccination]
     */
    public fun getLatestValidVaccination(): CombinedCovCertificate? =
        getLatestValidVaccinations().firstOrNull()

    /**
     * @return The latest valid [CombinedCovCertificate]'s that are [Vaccination]
     */
    public fun getLatestValidVaccinations(): List<CombinedCovCertificate> {
        return certificates.filter { it.covCertificate.dgcEntry is Vaccination && !it.isExpiredOrRevoked() }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence,
                ) ?: 0
            }
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Recovery]
     */
    public fun getLatestRecovery(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is Recovery }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult,
                ) ?: 0
            }.firstOrNull()
    }

    /**
     * @return The latest valid [CombinedCovCertificate] that is a [Recovery]
     */
    public fun getLatestValidRecovery(): CombinedCovCertificate? =
        getLatestValidRecoveries().firstOrNull()

    /**
     * @return The latest valid [CombinedCovCertificate]'s that are [Recovery]
     */
    public fun getLatestValidRecoveries(): List<CombinedCovCertificate> {
        return certificates.filter { it.covCertificate.dgcEntry is Recovery && !it.isExpiredOrRevoked() }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult?.compareTo(
                    (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult,
                ) ?: 0
            }
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [TestCert]
     */
    public fun getLatestTest(): CombinedCovCertificate? {
        return certificates.filter { it.covCertificate.dgcEntry is TestCert }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? TestCert)?.sampleCollection?.compareTo(
                    (cert1.covCertificate.dgcEntry as? TestCert)?.sampleCollection,
                ) ?: 0
            }.firstOrNull()
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [TestCert]
     */
    public fun getLatestValidTest(): CombinedCovCertificate? = getLatestValidTests().firstOrNull()

    /**
     * @return The latest [CombinedCovCertificate]'s that are [TestCert]
     */
    public fun getLatestValidTests(): List<CombinedCovCertificate> {
        return certificates.filter { it.covCertificate.dgcEntry is TestCert && !it.isExpiredOrRevoked() }
            .sortedWith { cert1, cert2 ->
                (cert2.covCertificate.dgcEntry as? TestCert)?.sampleCollection?.compareTo(
                    (cert1.covCertificate.dgcEntry as? TestCert)?.sampleCollection,
                ) ?: 0
            }
    }

    public fun validateBoosterReissue() {
        val boosterAndVaccinationAndRecoveryIds =
            getBoosterAfterVaccinationAfterRecoveryIds(certificates)

        val listIds: List<String> = when {
            boosterAndVaccinationAndRecoveryIds.isNotEmpty() -> boosterAndVaccinationAndRecoveryIds
            else -> return
        }

        certificates = certificates.map {
            if (
                listIds.contains(it.covCertificate.dgcEntry.id) &&
                it.covCertificate.dgcEntry is Vaccination &&
                (it.covCertificate.dgcEntry as Vaccination).isCompleteDoubleDose
            ) {
                it.copy(
                    reissueState = ReissueState.Ready,
                    reissueType = ReissueType.Booster,
                )
            } else {
                it
            }
        }.toMutableList()
    }

    public fun validateExpiredReissue() {
        val expiredGermanVaccinationId = getExpiredGermanVaccinationId(getLatestVaccination())
        val expiredGermanRecoveryIds = getExpiredGermanRecoveryIds(certificates)
        val expiredNotGermanVaccinationId = getExpiredNotGermanVaccinationId(getLatestVaccination())
        val expiredNotGermanRecoveryIds = getExpiredNotGermanRecoveryIds(certificates)
        val expiredGermanAfter90DaysVaccinationId =
            getExpiredGermanAfter90DaysVaccinationId(getLatestVaccination())
        val expiredGermanAfter90DaysRecoveryIds =
            getExpiredGermanAfter90DaysRecoveryIds(certificates)
        if (
            expiredGermanVaccinationId == null &&
            expiredGermanRecoveryIds.isEmpty() &&
            expiredNotGermanVaccinationId == null &&
            expiredNotGermanRecoveryIds.isEmpty() &&
            expiredGermanAfter90DaysVaccinationId == null &&
            expiredGermanAfter90DaysRecoveryIds.isEmpty()
        ) {
            return
        }

        certificates = certificates.map {
            when {
                it.isRecoveryValidForReissue(expiredGermanRecoveryIds) -> {
                    it.copy(
                        reissueState = ReissueState.Ready,
                        reissueType = ReissueType.Recovery,
                    )
                }
                it.isRecoveryValidForReissue(expiredNotGermanRecoveryIds) -> {
                    it.copy(
                        reissueState = ReissueState.NotGermanReady,
                        reissueType = ReissueType.Recovery,
                    )
                }
                it.isRecoveryValidForReissue(expiredGermanAfter90DaysRecoveryIds) -> {
                    it.copy(
                        reissueState = ReissueState.AfterTimeLimit,
                        reissueType = ReissueType.Recovery,
                    )
                }
                expiredGermanVaccinationId == it.covCertificate.dgcEntry.id -> {
                    it.copy(
                        reissueState = ReissueState.Ready,
                        reissueType = ReissueType.Vaccination,
                    )
                }
                expiredNotGermanVaccinationId == it.covCertificate.dgcEntry.id -> {
                    it.copy(
                        reissueState = ReissueState.NotGermanReady,
                        reissueType = ReissueType.Vaccination,
                    )
                }
                expiredGermanAfter90DaysVaccinationId == it.covCertificate.dgcEntry.id -> {
                    it.copy(
                        reissueState = ReissueState.AfterTimeLimit,
                        reissueType = ReissueType.Vaccination,
                    )
                }
                else -> {
                    it
                }
            }
        }.toMutableList()
    }

    private fun CombinedCovCertificate.isRecoveryValidForReissue(
        expiredGermanRecoveryIds: List<String>,
    ) =
        expiredGermanRecoveryIds.contains(covCertificate.dgcEntry.id) &&
            !certificates.any {
                it.covCertificate.dgcEntry is Vaccination &&
                    (it.covCertificate.dgcEntry as Vaccination).occurrence ==
                    (covCertificate.dgcEntry as Recovery).firstResult
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
            (
                it.reissueState == ReissueState.Ready ||
                    it.reissueState == ReissueState.AfterTimeLimit ||
                    it.reissueState == ReissueState.NotGermanReady
                ) &&
                (
                    (
                        it.reissueType == ReissueType.Vaccination &&
                            getMainCertificate() == getLatestVaccination()
                        ) ||
                        it.reissueType == ReissueType.Recovery
                    )
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

        if (
            (
                latestVaccination.reissueState == ReissueState.Ready ||
                    latestVaccination.reissueState == ReissueState.NotGermanReady ||
                    latestVaccination.reissueState == ReissueState.AfterTimeLimit
                ) &&
            latestVaccination.reissueType == ReissueType.Vaccination
        ) {
            list.add(latestVaccination.covCertificate.dgcEntry.id)
        } else {
            return emptyList()
        }

        list.addAll(getHistoricalDataForDcc(latestVaccination.covCertificate.dgcEntry.id))
        return list
    }

    public fun getListOfVaccinationIdsReadyForReissueNeedNotification(): List<String> {
        val list = mutableListOf<String>()
        val latestVaccination = getLatestVaccination() ?: return emptyList()

        if (
            latestVaccination.reissueState == ReissueState.Ready &&
            latestVaccination.reissueType == ReissueType.Vaccination &&
            !latestVaccination.hasSeenExpiredReissueNotification
        ) {
            list.add(latestVaccination.covCertificate.dgcEntry.id)
        } else {
            return emptyList()
        }

        list.addAll(getHistoricalDataForDcc(latestVaccination.covCertificate.dgcEntry.id))
        return list
    }

    public fun getListOfRecoveryIdAndHistoryForReissue(): List<String> {
        val list = mutableListOf<String>()
        val recoveryId = certificates
            .filter {
                (it.reissueState == ReissueState.Ready) &&
                    it.covCertificate.dgcEntry is Recovery &&
                    !it.hasSeenExpiredReissueNotification
            }
            .map { it.covCertificate.dgcEntry.id }.firstOrNull()

        if (recoveryId.isNullOrEmpty()) {
            return emptyList()
        } else {
            list.add(recoveryId)
        }

        list.addAll(getHistoricalDataForDcc(recoveryId))
        return list
    }

    public fun getListOfNotGermanIds(): List<String> =
        certificates
            .filter {
                (it.reissueState == ReissueState.NotGermanReady) &&
                    !it.hasSeenReissueNotification
            }
            .map { it.covCertificate.dgcEntry.id }

    public fun getHistoricalDataForDcc(id: String): List<String> {
        val covCertificate = certificates.find {
            it.covCertificate.dgcEntry.id == id
        }?.covCertificate

        val filteredCertificates = getFilteredForHistoricalData(covCertificate, id)
        val vaccinationList = getFilteredVaccinationForHistoricalData(filteredCertificates)
        val recoveryList =
            getFilteredRecoveryForHistoricalData(filteredCertificates, vaccinationList)

        return (vaccinationList + recoveryList).sortedWith { cert1, cert2 ->
            val date1 = (cert1.covCertificate.dgcEntry as? Vaccination)?.occurrence
                ?: (cert1.covCertificate.dgcEntry as? Recovery)?.firstResult
            val date2 = (cert2.covCertificate.dgcEntry as? Vaccination)?.occurrence
                ?: (cert2.covCertificate.dgcEntry as? Recovery)?.firstResult
            date1?.compareTo(date2) ?: 0
        }.take(5).map {
            it.covCertificate.dgcEntry.id
        }.toList()
    }

    private fun getFilteredForHistoricalData(covCertificate: CovCertificate?, id: String) =
        certificates.filterNot {
            it.covCertificate.dgcEntry.id == id
        }.filterNot {
            it.covCertificate.dgcEntry is TestCert
        }.filter {
            val date1 = (it.covCertificate.dgcEntry as? Vaccination)?.occurrence
                ?: (it.covCertificate.dgcEntry as? Recovery)?.firstResult
            val date2 = (covCertificate?.dgcEntry as? Vaccination)?.occurrence
                ?: (covCertificate?.dgcEntry as? Recovery)?.firstResult
            date1?.isBefore(date2) ?: false
        }

    private fun getFilteredVaccinationForHistoricalData(
        filteredCertificates: List<CombinedCovCertificate>,
    ): List<CombinedCovCertificate> {
        val vaccinationDateList = filteredCertificates.filter {
            it.covCertificate.dgcEntry is Vaccination
        }.distinctBy {
            (it.covCertificate.dgcEntry as Vaccination).occurrence
        }.map {
            (it.covCertificate.dgcEntry as Vaccination).occurrence
        }
        val vaccinationList: MutableList<CombinedCovCertificate> = mutableListOf()
        vaccinationDateList.forEach { date ->
            vaccinationList.add(
                filteredCertificates.filter {
                    it.covCertificate.dgcEntry is Vaccination &&
                        (it.covCertificate.dgcEntry as Vaccination).occurrence == date
                }.asSequence().sortedByDescending {
                    it.covCertificate.validFrom
                }.first(),
            )
        }
        return vaccinationList
    }

    private fun getFilteredRecoveryForHistoricalData(
        filteredCertificates: List<CombinedCovCertificate>,
        vaccinationList: List<CombinedCovCertificate>,
    ): List<CombinedCovCertificate> {
        val recoveryDateList = filteredCertificates.filter {
            it.covCertificate.dgcEntry is Recovery
        }.distinctBy {
            (it.covCertificate.dgcEntry as Recovery).firstResult
        }.map {
            (it.covCertificate.dgcEntry as Recovery).firstResult
        }
        val recoveryList: MutableList<CombinedCovCertificate> = mutableListOf()
        recoveryDateList.forEach { date ->
            recoveryList.add(
                filteredCertificates.filter {
                    it.covCertificate.dgcEntry is Recovery &&
                        (it.covCertificate.dgcEntry as Recovery).firstResult == date
                }.asSequence().sortedByDescending {
                    it.covCertificate.validFrom
                }.first(),
            )
        }
        return recoveryList.filterNot { recovery ->
            vaccinationList.any { vaccination ->
                (vaccination.covCertificate.dgcEntry as? Vaccination)?.occurrence ==
                    (recovery.covCertificate.dgcEntry as? Recovery)?.firstResult
            }
        }
    }

    public fun getListOfRecoveryIdsReadyForReissue(): List<String> {
        return certificates
            .filter {
                it.reissueState == ReissueState.Ready &&
                    it.covCertificate.dgcEntry is Recovery
            }
            .map { it.covCertificate.dgcEntry.id }
    }

    public fun getListOfIdsReadyForBoosterReissue(): List<String> {
        val list = mutableListOf<String>()
        val vaccinationIdToReissue = certificates
            .filter {
                it.reissueState == ReissueState.Ready &&
                    it.covCertificate.dgcEntry is Vaccination &&
                    (it.covCertificate.dgcEntry as Vaccination).doseNumber == 2
            }
            .map { it.covCertificate.dgcEntry.id }

        list.addAll(vaccinationIdToReissue)
        list.addAll(getHistoricalDataForDcc(vaccinationIdToReissue.first()))

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

    public fun isCertVaccinationNotBoosterAfterJanssen(covCertificate: CovCertificate): Boolean {
        val dgcEntry = covCertificate.dgcEntry
        if (dgcEntry is Vaccination && !dgcEntry.isJanssen && dgcEntry.doseNumber == 2) {
            val vaccinationJanssenBeforeCovCertificate = certificates.find {
                it.covCertificate.dgcEntry is Vaccination &&
                    (it.covCertificate.dgcEntry as Vaccination).isJanssen &&
                    (it.covCertificate.dgcEntry as? Vaccination)?.occurrence?.isBefore(
                        (covCertificate.dgcEntry as? Vaccination)?.occurrence,
                    ) ?: false
            } ?: return false

            certificates.find {
                it.covCertificate.dgcEntry is Recovery &&
                    (it.covCertificate.dgcEntry as? Recovery)?.firstResult?.isBefore(
                        (vaccinationJanssenBeforeCovCertificate.covCertificate.dgcEntry as? Vaccination)?.occurrence,
                    ) ?: false
            } ?: return true

            return false
        }
        return false
    }

    public fun getMergedCertificate(
        latestVaccination: CombinedCovCertificate?,
        latestRecovery: CombinedCovCertificate?,
        latestTest: CombinedCovCertificate?,
    ): CombinedCovCertificate? {
        return when {
            latestVaccination != null -> {
                latestVaccination.copy(
                    covCertificate = latestVaccination.covCertificate.copy(
                        recoveries = latestRecovery?.covCertificate?.recovery?.let { listOf(it) },
                        tests = latestTest?.covCertificate?.test?.let { listOf(it) },
                    ),
                )
            }
            latestRecovery != null -> {
                latestRecovery.copy(
                    covCertificate = latestRecovery.covCertificate.copy(
                        tests = latestTest?.covCertificate?.test?.let { listOf(it) },
                    ),
                )
            }
            latestTest != null -> {
                latestTest
            }
            else -> {
                null
            }
        }
    }
}

public enum class ImmunizationStatus {
    Full, Partial, Invalid
}
