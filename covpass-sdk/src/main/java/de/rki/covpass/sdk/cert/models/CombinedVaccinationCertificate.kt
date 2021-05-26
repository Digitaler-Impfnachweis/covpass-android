/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable

/**
 * Data model which contains a [VaccinationCertificate] and additionally the raw qr string.
 */
@Serializable
public data class CombinedVaccinationCertificate(

    /** The [VaccinationCertificate]. */
    val vaccinationCertificate: VaccinationCertificate,

    /**
     * The raw qr content of the [VaccinationCertificate].
     */
    val vaccinationQrContent: String,
)
