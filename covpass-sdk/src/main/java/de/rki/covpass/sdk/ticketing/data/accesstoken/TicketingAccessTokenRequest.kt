/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.accesstoken

import kotlinx.serialization.Serializable

@Serializable
public data class TicketingAccessTokenRequest(
    val service: String,
    val pubKey: String,
)
