/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.accesstoken

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TicketingAccessTokenResponse(
    val jti: String? = null,
    val iss: String,
    val iat: Long,
    val sub: String,
    @SerialName("aud")
    val validationUrl: String,
    @SerialName("exp")
    val exp: Long,
    @SerialName("t")
    val type: Long,
    val v: String,
    @SerialName("vc")
    val certificateData: TicketingCertificateDataRemote,
)
