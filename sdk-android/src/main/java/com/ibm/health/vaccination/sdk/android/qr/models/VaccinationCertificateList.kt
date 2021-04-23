package com.ibm.health.vaccination.sdk.android.qr.models

import kotlinx.serialization.Serializable

/**
 * Data model which contains a list of [ExtendedVaccinationCertificate] and a pointer to the favorite / own certificate.
 */
@Serializable
public data class VaccinationCertificateList(
    var certificates: MutableList<ExtendedVaccinationCertificate> = mutableListOf(),
    var favoriteCertId: String? = null,
) {

    public fun addCertificate(certificate: ExtendedVaccinationCertificate) {
        certificates.add(certificate)
        if (certificates.size == 1) {
            favoriteCertId = certificate.vaccinationCertificate.id
        }
    }
}
