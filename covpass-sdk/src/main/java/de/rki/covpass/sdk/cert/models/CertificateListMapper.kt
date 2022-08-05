package de.rki.covpass.sdk.cert.models

import COSE.CoseException
import de.rki.covpass.sdk.cert.BadCoseSignatureException
import de.rki.covpass.sdk.cert.BlacklistedEntityException
import de.rki.covpass.sdk.cert.ExpiredCwtException
import de.rki.covpass.sdk.cert.QRCoder
import de.rki.covpass.sdk.cert.validateEntity
import java.security.GeneralSecurityException

/** Maps between [CovCertificateList] and [GroupedCertificatesList]. */
public class CertificateListMapper(
    private val qrCoder: QRCoder,
) {
    /** Transforms a [CovCertificateList] into a [GroupedCertificatesList]. */
    public fun toGroupedCertificatesList(covCertificateList: CovCertificateList): GroupedCertificatesList {
        val groupedCertificatesList = GroupedCertificatesList()
        for (localCert in covCertificateList.certificates) {
            var covCertificate: CovCertificate? = null
            val error = runCatching {
                covCertificate = qrCoder.decodeCovCert(localCert.qrContent, allowExpiredCertificates = true)
                covCertificate?.let {
                    validateEntity(it.dgcEntry.idWithoutPrefix)
                }
            }.exceptionOrNull()
            val status = when (error) {
                null ->
                    when {
                        localCert.isRevoked -> CertValidationResult.Revoked
                        localCert.covCertificate.isExpired() -> CertValidationResult.Expired
                        localCert.covCertificate.isInExpiryPeriod() -> CertValidationResult.ExpiryPeriod
                        else -> CertValidationResult.Valid
                    }
                is ExpiredCwtException -> {
                    certificateStatusInExpiredCwtException(localCert)
                }
                is BadCoseSignatureException,
                is CoseException,
                is GeneralSecurityException,
                is BlacklistedEntityException, -> CertValidationResult.Invalid
                else -> CertValidationResult.Invalid
            }
            val combinedCovCertificate = covCertificate.let {
                if (it != null &&
                    (localCert.covCertificate.kid.isEmpty() || localCert.covCertificate.rValue.isEmpty())
                ) {
                    localCert.copy(
                        covCertificate = localCert.covCertificate.copy(
                            kid = it.kid,
                            rValue = it.rValue,
                        ),
                    )
                } else {
                    localCert
                }
            }.toCombinedCovCertificate(status)
            groupedCertificatesList.addNewCertificate(combinedCovCertificate)
        }

        groupedCertificatesList.favoriteCertId = covCertificateList.favoriteCertId
        return groupedCertificatesList
    }

    private fun certificateStatusInExpiredCwtException(localCert: CombinedCovCertificateLocal): CertValidationResult =
        if (localCert.isRevoked) {
            CertValidationResult.Invalid
        } else {
            val blacklistedEntityError = runCatching {
                validateEntity(localCert.covCertificate.dgcEntry.idWithoutPrefix)
            }.exceptionOrNull()
            when (blacklistedEntityError) {
                null -> CertValidationResult.Expired
                else -> CertValidationResult.Invalid
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
