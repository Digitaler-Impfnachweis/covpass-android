/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing

import de.rki.covpass.sdk.reissuing.local.CertificateReissue
import de.rki.covpass.sdk.reissuing.local.CertificateReissueExecutionType
import de.rki.covpass.sdk.reissuing.mappers.toCertificateReissue

public class ReissuingRepository(
    private val reissuingApiService: ReissuingApiService,
) {
    public suspend fun reissueCertificate(
        certificates: List<String>,
        action: CertificateReissueExecutionType = CertificateReissueExecutionType.RENEW,
    ): CertificateReissue =
        reissuingApiService.reissueCertificate(
            action.name.lowercase(),
            certificates,
        ).first().toCertificateReissue()
}
