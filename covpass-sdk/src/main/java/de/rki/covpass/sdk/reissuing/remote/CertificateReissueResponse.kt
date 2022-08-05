/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing.remote

import kotlinx.serialization.Serializable

@Serializable
public data class CertificateReissueResponse(
    val certificate: String,
    val relations: List<CertificateReissueRelation>,
)

@Serializable
public data class CertificateReissueRelation(
    val index: Int,
    val action: String,
)
