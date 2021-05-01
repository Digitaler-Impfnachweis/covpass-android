package com.ibm.health.vaccination.app.vaccinee.storage

import com.ibm.health.vaccination.sdk.android.cert.models.ExtendedVaccinationCertificate
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificateList

/**
 * Data model which contains a list of [GroupedCertificates] and a pointer to the favorite / own certificate.
 * This data model is used at runtime, while for the persistent data the [VaccinationCertificateList] is used.
 * So for storing / loading, the two models have to be transformed into each other.
 */
// FIXME Clean this up. Separate data from logic. Rethink the back and forth conversion in addCertificate.
//  Think about immutability vs mutability, safety and efficiency (we hold this in an observable singleton). Etc.
// TODO maybe move this to sdk later on
data class GroupedCertificatesList(
    var certificates: MutableList<GroupedCertificates> = mutableListOf(),
    var favoriteCertId: String? = null,
) {

    fun getGroupedCertificates(certId: String): GroupedCertificates? {
        return certificates.firstOrNull {
            it.matchesId(certId)
        }
    }

    fun addCertificate(certificate: ExtendedVaccinationCertificate) {
        val vaccinationCertificateList = toVaccinationCertificateList()
        vaccinationCertificateList.addCertificate(
            certificate
        )
        val convertedGroupedCertificatesList = fromVaccinationCertificateList(vaccinationCertificateList)
        favoriteCertId = convertedGroupedCertificatesList.favoriteCertId
        certificates = convertedGroupedCertificatesList.certificates
    }

    fun deleteCertificate(certId: String) {
        certificates.removeIf {
            it.matchesId(certId)
        }
    }

    fun toggleFavorite(certId: String) {
        if (certId == favoriteCertId) {
            favoriteCertId = null
        } else {
            favoriteCertId = certId
        }
    }

    fun isMarkedAsFavorite(certId: String): Boolean = certId == favoriteCertId

    fun getSortedCertificates(): List<GroupedCertificates> {
        val sortedCerts = certificates.toMutableList()
        val favoriteIndex = sortedCerts.indexOfFirst {
            isMarkedAsFavorite(it.getMainCertId())
        }
        if (favoriteIndex >= 0) {
            val favoriteCert = sortedCerts.get(favoriteIndex)
            sortedCerts.remove(favoriteCert)
            sortedCerts.add(0, favoriteCert)
        }
        return sortedCerts
    }

    fun toVaccinationCertificateList(): VaccinationCertificateList {
        val singleCertList = mutableListOf<ExtendedVaccinationCertificate>()
        certificates.forEach { groupedCerts ->
            groupedCerts.incompleteCertificate?.let { singleCertList.add(it) }
            groupedCerts.completeCertificate?.let { singleCertList.add(it) }
        }
        return VaccinationCertificateList(singleCertList, favoriteCertId)
    }

    companion object {

        /**
         * Transforms a [VaccinationCertificateList] into a [GroupedCertificatesList].
         */
        fun fromVaccinationCertificateList(
            vaccinationCertificateList: VaccinationCertificateList
        ): GroupedCertificatesList {
            val certList = vaccinationCertificateList.certificates.toMutableList()
            val groupedCertificates = mutableListOf<GroupedCertificates>()
            var currentIndex = 0
            while (currentIndex < certList.size) {
                // Adds a grouped certificate for every original certificate. If a match is found, the matching element
                // is removed, so it cannot be checked again
                groupedCertificates.add(createGroupedCertificates(certList, currentIndex))
                currentIndex++
            }

            // Ensure that the favorite id always points on the main cert
            var favoriteId = vaccinationCertificateList.favoriteCertId
            if (favoriteId != null) {
                groupedCertificates.forEach {
                    if (it.isComplete() && it.incompleteCertificate?.vaccinationCertificate?.id == favoriteId) {
                        favoriteId = it.getMainCertId()
                        return@forEach
                    }
                }
            }

            return GroupedCertificatesList(groupedCertificates, favoriteId)
        }

        private fun createGroupedCertificates(
            certList: MutableList<ExtendedVaccinationCertificate>,
            startIndex: Int
        ): GroupedCertificates {
            val startCert = certList.get(startIndex)
            val startVaccinationCert = startCert.vaccinationCertificate
            var currentIndex = startIndex + 1
            while (currentIndex < certList.size) {
                // Check all remaining certs for matches
                val currentCert = certList.get(currentIndex)
                val currentVaccinationCert = currentCert.vaccinationCertificate
                val equalNames = currentVaccinationCert.name == startVaccinationCert.name
                val equalDates = currentVaccinationCert.birthDate == startVaccinationCert.birthDate
                val equalCompletion = currentVaccinationCert.isComplete == startVaccinationCert.isComplete
                val matchFound = equalNames && equalDates && !equalCompletion
                if (matchFound) {
                    // remove the matching cert, so it cannot be checked again
                    certList.removeAt(currentIndex)
                    val completeCert =
                        if (startVaccinationCert.isComplete) startCert else currentCert
                    val incompleteCert =
                        if (startVaccinationCert.isComplete) currentCert else startCert
                    return GroupedCertificates(completeCert, incompleteCert)
                }
                currentIndex++
            }
            // If not yet returned, no matching cert was found
            return if (startVaccinationCert.isComplete) {
                GroupedCertificates(startCert, null)
            } else {
                GroupedCertificates(null, startCert)
            }
        }
    }
}
