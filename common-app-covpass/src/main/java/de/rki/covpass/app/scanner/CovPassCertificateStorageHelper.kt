/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.cert.models.*

public object CovPassCertificateStorageHelper {

    public suspend fun addNewCertificate(
        groupedCertificatesList: SuspendMutableValueFlow<GroupedCertificatesList>,
        covCertificate: CovCertificate,
        qrContent: String
    ): GroupedCertificatesId? {
        var certId: GroupedCertificatesId? = null
        groupedCertificatesList.update {
            certId = it.addNewCertificate(
                CombinedCovCertificate(
                    covCertificate = covCertificate,
                    qrContent = qrContent,
                    timestamp = System.currentTimeMillis(),
                    status = if (covCertificate.isInExpiryPeriod()) {
                        CertValidationResult.ExpiryPeriod
                    } else {
                        CertValidationResult.Valid
                    },
                    hasSeenBoosterNotification = false,
                    hasSeenBoosterDetailNotification = false,
                    hasSeenExpiryNotification = false,
                    boosterNotificationRuleIds = mutableListOf(),
                    isReadyForReissue = false,
                    alreadyReissued = false,
                    hasSeenReissueNotification = false,
                    hasSeenReissueDetailNotification = false,
                )
            )
        }
        return certId
    }
}
