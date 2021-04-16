package com.ibm.health.vaccination.sdk.android.qr.models

import kotlinx.serialization.Serializable

/**
 * Data model which contains a [VaccinationCertificate] and additionally the raw validation qr content.
 */
@Serializable
public data class ExtendedVaccinationCertificate(

    /** The [VaccinationCertificate]. */
    val vaccinationCertificate: VaccinationCertificate,

    /**
     * The raw qr content of the full vaccination certificate. This is not really used right now, but stored as a
     * safety fallback in case of e.g. data migration issues.
     */
    val vaccinationQrContent: String,

    /** The raw qr content of the simplified validation certificate. */
    val validationQrContent: String?,
)
