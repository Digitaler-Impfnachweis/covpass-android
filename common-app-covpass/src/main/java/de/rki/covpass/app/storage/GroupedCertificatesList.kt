/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.sdk.android.cert.models.CombinedVaccinationCertificate
import de.rki.covpass.sdk.android.cert.models.VaccinationCertificate
import de.rki.covpass.sdk.android.cert.models.VaccinationCertificateList

/**
 * Data model which contains a list of [GroupedCertificates] and a pointer to the favorite / own certificate.
 * This data model is used at runtime, while for the persistent data the [VaccinationCertificateList] is used.
 * So for storing / loading, the two models have to be transformed into each other.
 */
internal data class GroupedCertificatesList(
    var certificates: MutableList<GroupedCertificates> = mutableListOf(),
    var favoriteCertId: String? = null,
) {

    /**
     * Returns a [GroupedCertificates] if the id of the incomplete or complete cert matches the given [certId].
     */
    fun getGroupedCertificates(certId: String): GroupedCertificates? =
        certificates.firstOrNull { it.matchesId(certId) }

    /**
     * Returns the [CombinedVaccinationCertificate] with the given [certId] if existent, else null.
     */
    fun getCombinedCertificate(certId: String): CombinedVaccinationCertificate? {
        certificates.forEach {
            if (it.completeCertificate?.vaccinationCertificate?.vaccination?.id == certId) {
                return it.completeCertificate
            } else if (it.incompleteCertificate?.vaccinationCertificate?.vaccination?.id == certId) {
                return it.incompleteCertificate
            }
        }
        return null
    }

    /**
     * Adds the given [CombinedVaccinationCertificate] to the [GroupedCertificatesList].
     * The [CombinedVaccinationCertificate] will be grouped together if some existing cert matches, else it will be
     * added as a single entry.
     */
    fun addCertificate(certificate: CombinedVaccinationCertificate) {
        val vaccinationCertificateList = toVaccinationCertificateList().apply {
            addCertificate(certificate)
        }
        val result = fromVaccinationCertificateList(vaccinationCertificateList)
        favoriteCertId = result.favoriteCertId
        certificates = result.certificates
    }

    /**
     * Deletes the [VaccinationCertificate] with the given id.
     * If this is the only cert, the [GroupedCertificates] is deleted.
     *
     * @return The new main certificate id if the [GroupedCertificates] was not deleted, else null.
     * @throws NoSuchElementException if the [certId] does not exist.
     */
    fun deleteVaccinationCertificate(certId: String): String? {
        var newMainCertId: String? = null
        val matchingGroupedCert = certificates.first {
            it.matchesId(certId)
        }
        if (matchingGroupedCert.isSingleVaccination()) {
            certificates.remove(matchingGroupedCert)
        } else {
            matchingGroupedCert.removeCert(certId)
            newMainCertId = matchingGroupedCert.getMainCertId()
        }
        if (favoriteCertId == certId) {
            favoriteCertId = newMainCertId
        }
        return newMainCertId
    }

    fun toggleFavorite(certId: String) {
        favoriteCertId = if (certId == favoriteCertId) {
            null
        } else {
            certId
        }
    }

    fun isMarkedAsFavorite(certId: String): Boolean =
        certId == favoriteCertId

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
        val singleCertList = mutableListOf<CombinedVaccinationCertificate>()
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
            vaccinationCertificateList: VaccinationCertificateList,
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
                    if (it.isComplete() &&
                        it.incompleteCertificate?.vaccinationCertificate?.vaccinations?.first()?.id == favoriteId
                    ) {
                        favoriteId = it.getMainCertId()
                        return@forEach
                    }
                }
            }

            return GroupedCertificatesList(groupedCertificates, favoriteId)
        }

        private fun createGroupedCertificates(
            certList: MutableList<CombinedVaccinationCertificate>,
            startIndex: Int,
        ): GroupedCertificates {
            val startCert = certList[startIndex]
            val startVaccinationCert = startCert.vaccinationCertificate
            var currentIndex = startIndex + 1
            while (currentIndex < certList.size) {
                // Check all remaining certs for matches
                val currentCert = certList[currentIndex]
                val currentVaccinationCert = currentCert.vaccinationCertificate
                val equalNames = currentVaccinationCert.fullName == startVaccinationCert.fullName
                val equalDates = currentVaccinationCert.birthDate == startVaccinationCert.birthDate
                val equalCompletion = currentVaccinationCert.isComplete == startVaccinationCert.isComplete
                val matchFound = equalNames && equalDates && !equalCompletion
                if (matchFound) {
                    // remove the matching cert, so it cannot be checked again
                    certList.removeAt(currentIndex)
                    val completeCert = if (startVaccinationCert.isComplete) startCert
                    else currentCert
                    val incompleteCert = if (startVaccinationCert.isComplete) currentCert
                    else startCert
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
