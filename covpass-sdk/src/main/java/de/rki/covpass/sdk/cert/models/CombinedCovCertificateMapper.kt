package de.rki.covpass.sdk.cert.models

import java.time.ZoneId
import java.time.ZonedDateTime

public fun CombinedCovCertificateLocal.toCombinedCovCertificate(
    status: CertValidationResult,
): CombinedCovCertificate =
    CombinedCovCertificate(
        covCertificate = covCertificate,
        qrContent = qrContent,
        timestamp = timestamp,
        status = status,
        hasSeenBoosterNotification = hasSeenBoosterNotification,
        hasSeenBoosterDetailNotification = hasSeenBoosterDetailNotification,
        hasSeenExpiryNotification = hasSeenExpiryNotification,
        boosterNotificationRuleIds = boosterNotificationRuleIds,
        isReadyForReissue = isReadyForReissue,
        alreadyReissued = alreadyReissued,
        hasSeenReissueNotification = hasSeenReissueNotification,
        hasSeenReissueDetailNotification = hasSeenReissueDetailNotification,
        hasSeenRevokedNotification = hasSeenRevokedNotification,
        isRevoked = isRevoked
    )

public fun CombinedCovCertificate.toCombinedCovCertificateLocal(): CombinedCovCertificateLocal =
    CombinedCovCertificateLocal(
        covCertificate = covCertificate,
        qrContent = qrContent,
        timestamp = timestamp,
        hasSeenBoosterNotification = hasSeenBoosterNotification,
        hasSeenBoosterDetailNotification = hasSeenBoosterDetailNotification,
        hasSeenExpiryNotification = hasSeenExpiryNotification,
        boosterNotificationRuleIds = boosterNotificationRuleIds,
        isReadyForReissue = isReadyForReissue,
        alreadyReissued = alreadyReissued,
        hasSeenReissueNotification = hasSeenReissueNotification,
        hasSeenReissueDetailNotification = hasSeenReissueDetailNotification,
        hasSeenRevokedNotification = hasSeenRevokedNotification,
        isRevoked = isRevoked
    )

public fun CovCertificate.isInExpiryPeriod(): Boolean =
    ZonedDateTime.ofInstant(
        validUntil,
        ZoneId.systemDefault()
    ).minusDays(28).isBefore(ZonedDateTime.now())

public fun CovCertificate.isExpired(): Boolean =
    ZonedDateTime.ofInstant(
        validUntil,
        ZoneId.systemDefault()
    ).isBefore(ZonedDateTime.now())
