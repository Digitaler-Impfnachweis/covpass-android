/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TicketingIdentityDocumentResponse(
    val id: String,
    @SerialName("verificationMethod")
    val verificationMethods: List<TicketingVerificationMethodRemote>,
    @SerialName("service")
    val serviceRemoteTicketing: List<TicketingServiceRemote>,
)
