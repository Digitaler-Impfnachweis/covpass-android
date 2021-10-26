/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model which contains a [CovCertificate] and additionally the raw qr string.
 */
@Serializable
public data class CombinedCovCertificateLocal(

    /** The [CovCertificate]. */
    @SerialName("vaccinationCertificate")
    val covCertificate: CovCertificate,

    /**
     * The raw qr content of the [CovCertificate].
     */
    @SerialName("vaccinationQrContent")
    val qrContent: String,

    /**
     * Timestamp when the certificate was scanned
     */
    @SerialName("certificateTimestamp")
    val timestamp: Long = Long.MIN_VALUE,

    val hasSeenBoosterNotification: Boolean = false,
    val hasSeenBoosterDetailNotification: Boolean = false,

    val hasSeenExpiryNotification: Boolean = false,

    val boosterNotificationRuleIds: List<String> = mutableListOf(),
)
