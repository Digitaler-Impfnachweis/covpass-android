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
    }

    public fun getExtendedVaccinationCertificate(certId: String): ExtendedVaccinationCertificate {
        return certificates.first {
            it.vaccinationCertificate.id == certId
        }
    }

    public fun deleteCertificate(certId: String) {
        certificates.removeIf {
            it.vaccinationCertificate.id == certId
        }
    }

    public fun toggleFavorite(certId: String) {
        if (certId == favoriteCertId) {
            favoriteCertId = null
        } else {
            favoriteCertId = certId
        }
    }

    public fun isMarkedAsFavorite(certId: String): Boolean = certId == favoriteCertId

    public fun getSortedCertificates(): List<ExtendedVaccinationCertificate> {
        val sortedCerts = certificates.toMutableList()
        val favoriteIndex = sortedCerts.indexOfFirst {
            isMarkedAsFavorite(it.vaccinationCertificate.id)
        }
        if (favoriteIndex >= 0) {
            val favoriteCert = sortedCerts.get(favoriteIndex)
            sortedCerts.remove(favoriteCert)
            sortedCerts.add(0, favoriteCert)
        }
        return sortedCerts
    }
}
