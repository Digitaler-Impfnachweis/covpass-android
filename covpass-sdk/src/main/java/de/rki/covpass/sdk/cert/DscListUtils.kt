/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.logging.Lumber
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.cert.models.DscListEntry
import de.rki.covpass.sdk.crypto.readPem

/**
 * Converts the list of [DscListEntry] to a list of [TrustedCert].
 */
public fun DscList.toTrustedCerts(): List<TrustedCert> {
    return certificates.mapNotNull { dscEntry ->
        val certificate = readPem(
            "-----BEGIN CERTIFICATE-----\n" +
                "${dscEntry.rawData}\n" +
                "-----END CERTIFICATE-----",
        ).firstOrNull()

        if (certificate == null) {
            Lumber.w { "DSC list contains invalid X509Certificate for kid ${dscEntry.kid}" }
            return@mapNotNull null
        }

        TrustedCert(
            country = dscEntry.country,
            kid = dscEntry.kid,
            certificate = certificate,
        )
    }
}
