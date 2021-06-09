/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.sdk.cert.models.*
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
            dgcEntry is Test &&
                dgcEntry.sampleCollection?.isOlderThan(48) == false &&
                dgcEntry.testType == Test.PCR_TEST &&
                dgcEntry.testResult == Test.NEGATIVE_RESULT
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Test &&
                dgcEntry.sampleCollection?.isOlderThan(24) == false &&
                dgcEntry.testType == Test.ANTIGEN_TEST &&
                dgcEntry.testResult == Test.NEGATIVE_RESULT
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination &&
                dgcEntry.hasFullProtection
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Recovery &&
                dgcEntry.validUntil?.isBefore(LocalDate.now()) == false
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination &&
                dgcEntry.isComplete
        } ?: certificates.find {
            it.covCertificate.dgcEntry is Vaccination
        } ?: certificates.find {
            it.covCertificate.dgcEntry is Recovery
        } ?: certificates.first()
    }
}
