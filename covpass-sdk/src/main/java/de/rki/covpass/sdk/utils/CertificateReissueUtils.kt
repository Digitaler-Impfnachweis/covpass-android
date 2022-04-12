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
        val recovery = certificates.find {
            it.covCertificate.dgcEntry is Recovery
        }?.covCertificate

        val singleDoseVaccination = certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isCompleteSingleDose
        }?.covCertificate

        val doubleDoseVaccination = certificates.find {
            val dgcEntry = it.covCertificate.dgcEntry
            dgcEntry is Vaccination && dgcEntry.isCompleteDoubleDose
        }?.covCertificate

        if (singleDoseVaccination == null || doubleDoseVaccination == null) {
            return emptyList()
        }

        if (recovery?.recovery?.firstResult?.isAfter(doubleDoseVaccination.vaccination?.occurrence) == true) {
            return emptyList()
        }

        val isGermanCertificate = listOfNotNull(
            recovery,
            singleDoseVaccination,
            doubleDoseVaccination
        ).all { it.isGermanCertificate }

        return if (isGermanCertificate) {
            listOf(
                recovery?.recovery?.id,
                singleDoseVaccination.vaccination?.id,
                doubleDoseVaccination.vaccination?.id
            ).mapNotNull { it }
        } else {
            emptyList()
        }
    }
}
