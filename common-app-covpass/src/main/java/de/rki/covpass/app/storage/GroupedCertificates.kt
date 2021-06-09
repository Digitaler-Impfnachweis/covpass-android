/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.commonapp.utils.CertificateHelper
import de.rki.covpass.commonapp.utils.CertificateType
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
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
            CertificateHelper.resolveCertificateType(dgcEntry) == CertificateType.NEGATIVE_PCR_TEST &&
                (dgcEntry as? Test)?.sampleCollection?.isOlderThan(48) == false
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            CertificateHelper.resolveCertificateType(dgcEntry) == CertificateType.NEGATIVE_ANTIGEN_TEST &&
                (dgcEntry as? Test)?.sampleCollection?.isOlderThan(24) == false
        } ?: certificates.find {
            CertificateHelper.resolveCertificateType(it.covCertificate.dgcEntry) ==
                CertificateType.VACCINATION_FULL_PROTECTION
        } ?: certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            CertificateHelper.resolveCertificateType(it.covCertificate.dgcEntry) == CertificateType.RECOVERY &&
                (dgcEntry as? Recovery)?.validUntil?.isBefore(LocalDate.now()) == false
        } ?: certificates.find {
            CertificateHelper.resolveCertificateType(it.covCertificate.dgcEntry) ==
                CertificateType.VACCINATION_COMPLETE
        } ?: certificates.find {
            CertificateHelper.resolveCertificateType(it.covCertificate.dgcEntry) ==
                CertificateType.VACCINATION_INCOMPLETE
        } ?: certificates.find {
            CertificateHelper.resolveCertificateType(it.covCertificate.dgcEntry) == CertificateType.RECOVERY
        } ?: certificates.first()
    }

    fun getSortedCertificates() = certificates.sortedByDescending { it.timestamp }
}
