/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.isOlderThan
import java.time.LocalDate

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
public data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
) {

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
     * Negative PCR Test not older then (=<)48h
     * #2 Test certificate
     * Negative quick test, not older then (=<) 24 hrs
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
     * Negative PCR-Test, older then (>) 48 Hrs, or negative quick test older then (>) 24 Hrs
     */
    public fun getMainCertificate(): CombinedCovCertificate {
        return certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Test && dgcEntry.type == TestCertType.NEGATIVE_PCR_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(48) == false
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Test && dgcEntry.type == TestCertType.NEGATIVE_ANTIGEN_TEST &&
                dgcEntry.sampleCollection?.isOlderThan(24) == false
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_FULL_PROTECTION
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Recovery && dgcEntry.validUntil?.isBefore(LocalDate.now()) == false
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_COMPLETE
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry.type == VaccinationCertType.VACCINATION_INCOMPLETE
        } ?: certificates.find {
            it.covCertificate.dgcEntry is Recovery
        } ?: certificates.first()
    }

    /**
     * @return The [certificates] sorted by the date of adding them. Most recently added one first.
     */
    public fun getSortedCertificates(): List<CombinedCovCertificate> = certificates.sortedByDescending { it.timestamp }
}
