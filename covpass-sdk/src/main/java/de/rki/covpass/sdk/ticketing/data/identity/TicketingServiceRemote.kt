/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

import kotlinx.serialization.Serializable

@Serializable
public data class TicketingServiceRemote(
    val id: String,
    val type: String,
    val serviceEndpoint: String,
    val name: String,
)
