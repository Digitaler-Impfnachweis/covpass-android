/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
// TODO maybe move this to sdk later on
internal data class GroupedCertificates(
    var certificates: MutableList<CombinedCovCertificate>,
) {

    val id: GroupedCertificatesId
        get() = GroupedCertificatesId(
            certificates.first().covCertificate.name, certificates.first().covCertificate.birthDate
        )

    // FIXME implement correct priority logic
    fun getMainCertificate() = certificates.first()
}
