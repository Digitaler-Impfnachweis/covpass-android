/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.utils.isOlderThan
import java.time.LocalDate

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
// TODO maybe move this to sdk later on
internal data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
) {

    val id: GroupedCertificatesId
        get() = GroupedCertificatesId(
            certificates.first().covCertificate.name, certificates.first().covCertificate.birthDate
        )

    fun getMainCertificate(): CombinedCovCertificate {
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

    fun getSortedCertificates() = certificates.sortedByDescending { it.timestamp }
}
