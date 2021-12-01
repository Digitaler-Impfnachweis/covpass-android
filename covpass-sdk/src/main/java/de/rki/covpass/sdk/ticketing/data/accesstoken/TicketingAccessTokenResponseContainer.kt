/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.accesstoken

public data class TicketingAccessTokenResponseContainer(
    val accessToken: TicketingAccessTokenResponse,
    val ticketingAccessTokenData: TicketingAccessTokenData,
)
