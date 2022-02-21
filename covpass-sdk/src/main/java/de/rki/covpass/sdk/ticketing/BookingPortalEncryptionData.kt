/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenResponseContainer
import de.rki.covpass.sdk.ticketing.data.identity.TicketingValidationServiceIdentityResponse
import java.security.KeyPair

public data class BookingPortalEncryptionData(
    val keyPair: KeyPair,
    val accessTokenContainer: TicketingAccessTokenResponseContainer,
    val ticketingValidationServiceIdentity: TicketingValidationServiceIdentityResponse,
    val validationServiceId: String,
    val cancellationServiceUrl: String?,
)
