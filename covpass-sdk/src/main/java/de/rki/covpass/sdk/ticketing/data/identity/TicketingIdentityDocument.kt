/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

public data class TicketingIdentityDocument(
    val accessTokenService: TicketingServiceRemote,
    val validationServices: List<TicketingServiceRemote>,
)
