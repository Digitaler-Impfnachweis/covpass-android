package de.rki.covpass.sdk.cert.models

import COSE.CoseException
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.utils.getDescriptionLanguage
import java.security.GeneralSecurityException

/** Maps between [CovCertificateList] and [GroupedCertificatesList]. */
public class CertificateListMapper(
    private val qrCoder: QRCoder,
    private val boosterRulesValidator: BoosterRulesValidator,
) {
    /** Transforms a [CovCertificateList] into a [GroupedCertificatesList]. */
    public suspend fun toGroupedCertificatesList(covCertificateList: CovCertificateList): GroupedCertificatesList {
        val groupedCertificatesList = GroupedCertificatesList()
        for (localCert in covCertificateList.certificates) {
            val error = runCatching {
                val covCertificate = qrCoder.decodeCovCert(localCert.qrContent)
                validateEntity(covCertificate.dgcEntry.idWithoutPrefix)
            }.exceptionOrNull()
            val status = when (error) {
                null ->
                    if (localCert.covCertificate.isInExpiryPeriod())
                        CertValidationResult.ExpiryPeriod
                    else
                        CertValidationResult.Valid
                is ExpiredCwtException -> CertValidationResult.Expired
                is BadCoseSignatureException,
                is CoseException,
                is GeneralSecurityException,
                is BlacklistedEntityException -> CertValidationResult.Invalid
                else -> CertValidationResult.Invalid
            }
            groupedCertificatesList.addNewCertificate(localCert.toCombinedCovCertificate(status))
        }

        for (groupedCert in groupedCertificatesList.certificates) {
            val latestVaccination = groupedCert.getLatestVaccination()?.covCertificate
            val latestRecovery = groupedCert.getLatestRecovery()?.covCertificate
            val recovery = latestRecovery?.recovery
            val boosterNotification = when {
                latestVaccination != null && recovery != null -> {
                    val mergedCertificate = latestVaccination.copy(
                        recoveries = listOf(recovery)
                    )
                    validateBoosterRules(boosterRulesValidator, mergedCertificate)
                }
                latestVaccination != null -> {
                    validateBoosterRules(boosterRulesValidator, latestVaccination)
                }
                else -> BoosterNotification(BoosterResult.Failed)
            }
            groupedCert.boosterNotification = boosterNotification
        }

        groupedCertificatesList.favoriteCertId = covCertificateList.favoriteCertId
        return groupedCertificatesList
    }

    private suspend fun validateBoosterRules(
        boosterRulesValidator: BoosterRulesValidator,
        covCertificate: CovCertificate,
    ): BoosterNotification {
        val boosterResult = boosterRulesValidator.validate(covCertificate).firstOrNull {
            it.result == de.rki.covpass.sdk.cert.BoosterResult.PASSED
        }
        return if (boosterResult != null) {
            BoosterNotification(
                BoosterResult.Passed,
                boosterResult.rule.getDescriptionFor(getDescriptionLanguage()),
                boosterResult.rule.identifier
            )
        } else {
            BoosterNotification(BoosterResult.Failed)
        }
    }

    /** Transforms a [GroupedCertificatesList] into a [CovCertificateList]. */
    public fun toCovCertificateList(groupedCertificatesList: GroupedCertificatesList): CovCertificateList {
        val certs = groupedCertificatesList.certificates.flatMap { groupedCerts ->
            groupedCerts.certificates.map { it.toCombinedCovCertificateLocal() }
        }
        return CovCertificateList(certs, groupedCertificatesList.favoriteCertId)
    }
}
