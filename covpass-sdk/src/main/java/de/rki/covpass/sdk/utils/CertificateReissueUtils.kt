/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination
import java.time.Instant
import java.time.temporal.ChronoUnit

public object CertificateReissueUtils {

    public fun getBoosterAfterVaccinationAfterRecoveryIds(
        certificates: List<CombinedCovCertificate>,
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
                    (cert1.dgcEntry as? Recovery)?.firstResult,
                ) ?: 0
            }
        } else {
            emptyList()
        }

        val isRecoveryLatest = sortedRecoveries.firstOrNull()?.recovery?.firstResult?.isAfter(
            doubleDoseVaccination.vaccination?.occurrence,
        ) == true

        val recoveriesOrEmpty = if (isRecoveryLatest) {
            emptyList()
        } else {
            if (sortedRecoveries.firstOrNull()?.isGermanCertificate == true) {
                sortedRecoveries
            } else {
                emptyList()
            }
        }

        val isGermanCertificate = (
            recoveriesOrEmpty +
                listOfNotNull(
                    singleDoseVaccination,
                    doubleDoseVaccination,
                )
            ).all { it.isGermanCertificate }

        return if (isGermanCertificate) {
            (
                recoveriesOrEmpty.mapNotNull { it.recovery?.id } +
                    listOfNotNull(
                        singleDoseVaccination.vaccination?.id,
                        doubleDoseVaccination.vaccination?.id,
                    )
                ).map { it }
        } else {
            emptyList()
        }
    }

    public fun getExpiredGermanVaccinationId(
        vaccination: CombinedCovCertificate?,
    ): String? {
        if (vaccination == null) return null
        val isExpiredGermanVaccinationCertificate = vaccination.isExpiredOrExpiryPeriod &&
            vaccination.covCertificate.isGermanCertificate &&
            vaccination.covCertificate.validUntil?.plus(90, ChronoUnit.DAYS)?.isAfter(Instant.now()) == true
        return if (isExpiredGermanVaccinationCertificate) {
            vaccination.covCertificate.dgcEntry.id
        } else {
            null
        }
    }

    public fun getExpiredGermanRecoveryIds(
        certificates: List<CombinedCovCertificate>,
    ): List<String> {
        val expiredGermanRecoveries = certificates.asSequence().filter {
            it.covCertificate.dgcEntry is Recovery && it.isExpiredOrExpiryPeriod &&
                it.covCertificate.isGermanCertificate &&
                it.covCertificate.validUntil?.plus(90, ChronoUnit.DAYS)?.isAfter(Instant.now()) == true
        }.sortedByDescending {
            it.covCertificate.validFrom
        }.distinctBy {
            (it.covCertificate.dgcEntry as Recovery).firstResult
        }.map { it.covCertificate }.filter {
            it.isGermanCertificate
        }.toList()

        return expiredGermanRecoveries.map { it.dgcEntry.id }
    }

    public fun getExpiredNotGermanVaccinationId(
        vaccination: CombinedCovCertificate?,
    ): String? {
        if (vaccination == null) return null
        val isExpiredNotGermanVaccinationCertificate = vaccination.isExpiredOrExpiryPeriod &&
            !vaccination.covCertificate.isGermanCertificate
        return if (isExpiredNotGermanVaccinationCertificate) {
            vaccination.covCertificate.dgcEntry.id
        } else {
            null
        }
    }

    public fun getExpiredNotGermanRecoveryIds(
        certificates: List<CombinedCovCertificate>,
    ): List<String> {
        val expiredNotGermanRecoveries = certificates.asSequence().filter {
            it.covCertificate.dgcEntry is Recovery && it.isExpiredOrExpiryPeriod &&
                !it.covCertificate.isGermanCertificate
        }.sortedByDescending {
            it.covCertificate.validFrom
        }.distinctBy {
            (it.covCertificate.dgcEntry as Recovery).firstResult
        }.map { it.covCertificate }.filter {
            it.isGermanCertificate
        }.toList()

        return expiredNotGermanRecoveries.map { it.dgcEntry.id }
    }

    public fun getExpiredGermanAfter90DaysVaccinationId(
        vaccination: CombinedCovCertificate?,
    ): String? {
        if (vaccination == null) return null
        val isExpiredAfter90DaysVaccinationCertificate = vaccination.isExpiredOrExpiryPeriod &&
            vaccination.covCertificate.isGermanCertificate &&
            vaccination.covCertificate.validUntil?.plus(90, ChronoUnit.DAYS)?.isBefore(Instant.now()) == true
        return if (isExpiredAfter90DaysVaccinationCertificate) {
            vaccination.covCertificate.dgcEntry.id
        } else {
            null
        }
    }

    public fun getExpiredGermanAfter90DaysRecoveryIds(
        certificates: List<CombinedCovCertificate>,
    ): List<String> {
        val expiredAfter90DaysRecoveries = certificates.asSequence().filter {
            it.covCertificate.dgcEntry is Recovery && it.isExpiredOrExpiryPeriod &&
                it.covCertificate.isGermanCertificate &&
                it.covCertificate.validUntil?.plus(90, ChronoUnit.DAYS)?.isBefore(Instant.now()) == true
        }.sortedByDescending {
            it.covCertificate.validFrom
        }.distinctBy {
            (it.covCertificate.dgcEntry as Recovery).firstResult
        }.map { it.covCertificate }.filter {
            it.isGermanCertificate
        }.toList()

        return expiredAfter90DaysRecoveries.map { it.dgcEntry.id }
    }

    private val CombinedCovCertificate.isExpiredOrExpiryPeriod: Boolean
        get() =
            status == CertValidationResult.Expired || status == CertValidationResult.ExpiryPeriod
}
