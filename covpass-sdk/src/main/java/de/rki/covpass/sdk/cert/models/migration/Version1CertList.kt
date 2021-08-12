/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models.migration

import de.rki.covpass.sdk.cert.models.CombinedCovCertificateLocal
import kotlinx.serialization.Serializable

/**
 * Outdated data model of data model version 1.
 */
@Serializable
@Deprecated("Outdated data model of data model version 1.")
public data class Version1CertList(
    val certificates: MutableList<CombinedCovCertificateLocal> = mutableListOf(),
    var favoriteCertId: String? = null,
)
