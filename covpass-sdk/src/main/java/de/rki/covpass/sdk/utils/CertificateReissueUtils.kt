/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination

public object CertificateReissueUtils {

    public fun getBoosterAfterVaccinationAfterRecoveryIds(
        certificates: List<CombinedCovCertificate>
    ): List<String> {
        val recoveryId = certificates.find {
            it.covCertificate.dgcEntry is Recovery
        }?.covCertificate?.dgcEntry?.id

        val vaccinationId = certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isCompleteSingleDose
        }?.covCertificate?.dgcEntry?.id

        val boosterId = certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isCompleteDoubleDose
        }?.covCertificate?.dgcEntry?.id

        if (vaccinationId == null || boosterId == null) return emptyList()
        return listOf(recoveryId, vaccinationId, boosterId).mapNotNull { it }
    }
}
