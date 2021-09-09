/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.cert.models.TestCert.Companion.ANTIGEN_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST_EXPIRY_TIME_HOURS
import de.rki.covpass.sdk.utils.isOlderThan
import java.time.LocalDate

public enum class BoosterResult {
    Passed,
    Failed
}

public data class BoosterNotification(
    val result: BoosterResult = BoosterResult.Failed,
    val description: String = "",
    val ruleId: String = "",
)

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
public data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
    var boosterNotification: BoosterNotification = BoosterNotification(),
) {

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
        val certificateSortedList = certificates.sortedWith { cert1, cert2 ->
            cert2.covCertificate.validFrom?.compareTo(cert1.covCertificate.validFrom) ?: 0
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
    public fun getSortedCertificates(): List<CombinedCovCertificate> = certificates.sortedByDescending { it.timestamp }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Vaccination]
     */
    public fun getLatestVaccination(): CombinedCovCertificate? {
        val sortedCertificates = certificates.sortedByDescending { it.covCertificate.validUntil }
        return sortedCertificates.find { it.covCertificate.dgcEntry is Vaccination }
    }

    /**
     * @return The latest [CombinedCovCertificate] that is a [Recovery]
     */
    public fun getLatestRecovery(): CombinedCovCertificate? {
        val sortedCertificates = certificates.sortedByDescending { it.covCertificate.validUntil }
        return sortedCertificates.find { it.covCertificate.dgcEntry is Recovery }
    }
}
