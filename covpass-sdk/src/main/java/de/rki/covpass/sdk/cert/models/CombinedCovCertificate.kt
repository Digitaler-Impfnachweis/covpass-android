/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable

/**
 * Data model which contains a [CovCertificate] and additionally the raw qr string.
 */
@Serializable
public data class CombinedCovCertificate(

    /** The [CovCertificate]. */
    val covCertificate: CovCertificate,

    /**
     * The raw qr content of the [CovCertificate].
     */
    val vaccinationQrContent: String,
)
