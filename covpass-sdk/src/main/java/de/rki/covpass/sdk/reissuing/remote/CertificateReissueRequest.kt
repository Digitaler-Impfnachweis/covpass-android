/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing.remote

import kotlinx.serialization.Serializable

@Serializable
public data class CertificateReissueRequest(
    val action: String,
    val certificates: List<String>,
)
