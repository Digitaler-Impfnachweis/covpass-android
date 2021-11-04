package de.rki.covpass.sdk.cert.models

import COSE.CoseException
import de.rki.covpass.sdk.cert.*
import java.security.GeneralSecurityException

/** Maps between [CovCertificateList] and [GroupedCertificatesList]. */
public class CertificateListMapper(
    private val qrCoder: QRCoder
) {
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

        groupedCertificatesList.favoriteCertId = covCertificateList.favoriteCertId
        return groupedCertificatesList
    }

    /** Transforms a [GroupedCertificatesList] into a [CovCertificateList]. */
    public fun toCovCertificateList(groupedCertificatesList: GroupedCertificatesList): CovCertificateList {
        val certs = groupedCertificatesList.certificates.flatMap { groupedCerts ->
            groupedCerts.certificates.map { it.toCombinedCovCertificateLocal() }
        }
        return CovCertificateList(certs, groupedCertificatesList.favoriteCertId)
    }
}
