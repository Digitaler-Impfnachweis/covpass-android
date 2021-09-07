package de.rki.covpass.sdk.cert.models

import COSE.CoseException
import de.rki.covpass.sdk.cert.*
import java.security.GeneralSecurityException

/** Maps between [CovCertificateList] and [GroupedCertificatesList]. */
public class CertificateListMapper(private val qrCoder: QRCoder) {
    /** Transforms a [CovCertificateList] into a [GroupedCertificatesList]. */
    public fun toGroupedCertificatesList(covCertificateList: CovCertificateList): GroupedCertificatesList {
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
            groupedCert.boosterResult = when {
                latestVaccination != null && recovery != null -> {
                    // uncomment and use mergedCertificate as parameter
//                    val mergedCertificate = latestVaccination.copy(
//                        recoveries = listOf(recovery)
//                    )
                    validateBoosterRules()
                }
                latestVaccination != null -> {
                    // use latestVaccination as parameter
                    validateBoosterRules()
                }
                latestRecovery != null -> {
                    // use latestRecovery as parameter
                    validateBoosterRules()
                }
                else -> {
                    BoosterResult.Failed
                }
            }
        }

        groupedCertificatesList.favoriteCertId = covCertificateList.favoriteCertId
        return groupedCertificatesList
    }

    // TODO add validation
    public fun validateBoosterRules(): BoosterResult {
        return BoosterResult.Passed
    }

    /** Transforms a [GroupedCertificatesList] into a [CovCertificateList]. */
    public fun toCovCertificateList(groupedCertificatesList: GroupedCertificatesList): CovCertificateList {
        val certs = groupedCertificatesList.certificates.flatMap { groupedCerts ->
            groupedCerts.certificates.map { it.toCombinedCovCertificateLocal() }
        }
        return CovCertificateList(certs, groupedCertificatesList.favoriteCertId)
    }
}
