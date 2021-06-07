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
    /** The current data model version used for data migration purposes. */
    val dataModelVersion: Int = CERT_DATA_MODEL_VERSION,
)

/**
 *  Model which contains only the version of [CovCertificateList].
 */
@Serializable
public data class CovCertificateListVersion(
    /** The data model version used for data migration purposes. The default is initial version 1.
     * When you deserialize this from storage, you will get the stored version from [CovCertificateList].
     */
    val dataModelVersion: Int = 1,
)

/** The current data model version used for data migration purposes. */
public const val CERT_DATA_MODEL_VERSION: Int = 2
