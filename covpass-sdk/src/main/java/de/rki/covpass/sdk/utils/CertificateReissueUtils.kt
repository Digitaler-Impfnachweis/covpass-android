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
        val recoveries = certificates.filter {
            it.covCertificate.dgcEntry is Recovery
        }.map { it.covCertificate }

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

        val sortedRecoveries = if (recoveries.isNotEmpty()) {
            recoveries.sortedWith { cert1, cert2 ->
                (cert2.dgcEntry as? Recovery)?.firstResult?.compareTo(
                    (cert1.dgcEntry as? Recovery)?.firstResult
                ) ?: 0
            }
        } else {
            emptyList()
        }

        val isRecoveryLatest = sortedRecoveries.firstOrNull()?.recovery?.firstResult?.isAfter(
            doubleDoseVaccination.vaccination?.occurrence
        ) == true

        val recoveriesOrEmpty = if (isRecoveryLatest) {
            emptyList()
        } else {
            sortedRecoveries
        }

        val isGermanCertificate = (
            recoveriesOrEmpty +
                listOfNotNull(
                    singleDoseVaccination,
                    doubleDoseVaccination
                )
            ).all { it.isGermanCertificate }

        return if (isGermanCertificate) {
            (
                recoveriesOrEmpty.mapNotNull { it.recovery?.id } +
                    listOfNotNull(
                        singleDoseVaccination.vaccination?.id,
                        doubleDoseVaccination.vaccination?.id
                    )
                ).map { it }
        } else {
            emptyList()
        }
    }
}
