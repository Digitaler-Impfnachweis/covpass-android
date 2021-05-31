/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable

/**
 * Data model which contains a list of [CombinedCovCertificate] and a pointer to the favorite / own certificate.
 */
@Serializable
public data class CovCertificateList(
    val certificates: MutableList<CombinedCovCertificate> = mutableListOf(),
    var favoriteCertId: GroupedCertificatesId? = null,
)
