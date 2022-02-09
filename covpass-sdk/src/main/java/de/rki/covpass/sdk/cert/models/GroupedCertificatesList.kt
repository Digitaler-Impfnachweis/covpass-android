/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import de.rki.covpass.sdk.utils.DccNameMatchingUtils.isHolderSame

/**
 * Data model which contains a list of [GroupedCertificates] and a pointer to the favorite / own certificate.
 * This data model is used at runtime, while for the persistent data the [CovCertificateList] is used.
 * So for storing / loading, the two models have to be transformed into each other.
 */
public data class GroupedCertificatesList private constructor(
    var certificates: MutableList<GroupedCertificates>,
    var favoriteCertId: GroupedCertificatesId?,
) {
    public constructor() : this(mutableListOf(), null)

    /**
     * Returns a [GroupedCertificates] if the id of the cert matches the given [certId].
     */
    public fun getGroupedCertificates(certId: GroupedCertificatesId): GroupedCertificates? =
        certificates.firstOrNull { it.id == certId }

    /**
     * Returns a list for [GroupedCertificates] except certificates with [CertValidationResult.Invalid] status.
     */
    public fun getValidCertificates(): List<GroupedCertificates> {
        return certificates.filterNot {
            it.getMainCertificate().status == CertValidationResult.Invalid
        }
    }

    /**
     * Returns the [CombinedCovCertificate] with the given [certId] if existent, else null.
     */
    public fun getCombinedCertificate(certId: String): CombinedCovCertificate? {
        certificates.forEach { groupedCert ->
            groupedCert.certificates.forEach { combinedCert ->
                if (combinedCert.covCertificate.dgcEntry.id == certId) {
                    return combinedCert
                }
            }
        }
        return null
    }

    /**
     * Adds the given [CombinedCovCertificate] to the [GroupedCertificatesList].
     * The [CombinedCovCertificate] will be grouped together if some existing cert matches, else it will be
     * added as a single entry.
     * If this is the first certificate added, it will be automatically set as favorite.
     *
     * @return The [GroupedCertificatesId] where the [addedCert] was added, or the freshly created
     * [GroupedCertificatesId].
     * @throws CertAlreadyExistsException If the cert was already added.
     */
    public fun addNewCertificate(addedCert: CombinedCovCertificate): GroupedCertificatesId {
        val certId = addCertificate(addedCert)
        if (certificates.size == 1) {
            favoriteCertId = certId
        }
        return certId
    }

    /**
     * @return The [GroupedCertificatesId] where the [addedCert] was added, or the freshly created
     * [GroupedCertificatesId].
     */
    private fun addCertificate(addedCert: CombinedCovCertificate): GroupedCertificatesId {
        if (addedCert.isPositivePcrOrAntigenTest()) {
            throw CertTestPositiveException()
        }
        var matchingGroupedCert: GroupedCertificates? = certificates.firstOrNull { groupedCerts ->
            isHolderSame(groupedCerts.certificates.first().covCertificate, addedCert.covCertificate)
        }
        val certDoesNotExistYet = certificates.none { groupedCerts ->
            groupedCerts.certificates.any { combinedCert ->
                combinedCert.covCertificate.dgcEntry.id == addedCert.covCertificate.dgcEntry.id
            }
        }
        if (certDoesNotExistYet) {
            if (matchingGroupedCert != null) {
                matchingGroupedCert.certificates.add(addedCert)
            } else {
                matchingGroupedCert = GroupedCertificates(mutableListOf(addedCert))
                certificates.add(matchingGroupedCert)
            }
            return matchingGroupedCert.id
        } else {
            throw CertAlreadyExistsException()
        }
    }

    /**
     * Deletes the [CovCertificate] with the given id.
     * If this is the only cert, the [GroupedCertificates] is deleted.
     *
     * @return True, if the complete [GroupedCertificates] was deleted, else false.
     */
    public fun deleteCovCertificate(certId: String): Boolean {
        var matchingGroupedCert: GroupedCertificates? = null
        var matchingCombinedCert: CombinedCovCertificate? = null
        certificates.forEach outer@{ groupedCert ->
            groupedCert.certificates.forEach { combinedCert ->
                if (combinedCert.covCertificate.dgcEntry.id == certId) {
                    matchingGroupedCert = groupedCert
                    matchingCombinedCert = combinedCert
                    return@outer
                }
            }
        }
        matchingGroupedCert?.certificates?.remove(matchingCombinedCert)
        if (matchingGroupedCert?.certificates?.isEmpty() == true) {
            certificates.remove(matchingGroupedCert)
            return true
        } else {
            return false
        }
    }

    /**
     * Sets the [certId] as favorite, or unfavorites the cert if it was already favorite.
     */
    public fun toggleFavorite(certId: GroupedCertificatesId) {
        favoriteCertId = if (certId == favoriteCertId) {
            null
        } else {
            certId
        }
    }

    /**
     * @return True if the [certId] is currently set as favorite.
     */
    public fun isMarkedAsFavorite(certId: GroupedCertificatesId): Boolean =
        certId == favoriteCertId

    /**
     * Moves the favorite cert to first position, the rest of the cert order is determined by when they were stored.
     */
    public fun getSortedCertificates(): List<GroupedCertificates> {
        val sortedCerts = certificates.toMutableList()
        val favoriteIndex = sortedCerts.indexOfFirst {
            isMarkedAsFavorite(it.id)
        }
        if (favoriteIndex >= 0) {
            val favoriteCert = sortedCerts.get(favoriteIndex)
            sortedCerts.remove(favoriteCert)
            sortedCerts.add(0, favoriteCert)
        }
        return sortedCerts
    }
}

/** This exception is thrown when certificates list already has such certificate */
public class CertAlreadyExistsException : RuntimeException()

/** This exception is thrown when the certificate is a positive PCR or Antigen test */
public class CertTestPositiveException : RuntimeException()
