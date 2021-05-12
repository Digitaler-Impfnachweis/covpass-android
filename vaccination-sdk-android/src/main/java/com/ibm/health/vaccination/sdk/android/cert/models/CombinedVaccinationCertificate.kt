package com.ibm.health.vaccination.sdk.android.cert.models

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
