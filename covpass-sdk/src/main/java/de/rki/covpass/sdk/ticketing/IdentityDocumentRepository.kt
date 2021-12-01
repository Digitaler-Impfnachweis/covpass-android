/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.ticketing.data.identity.ServiceType
import de.rki.covpass.sdk.ticketing.data.identity.TicketingIdentityDocument
import io.ktor.client.features.*
import io.ktor.http.*

public class IdentityDocumentRepository(
    public val ticketingApiService: TicketingApiService,
) {
    public suspend fun fetchIdentityDocument(
        ticketingDataInitialization: TicketingDataInitialization,
    ): TicketingIdentityDocument {
        try {
            val identityResponse = ticketingApiService.getIdentity(ticketingDataInitialization.serviceIdentity)
            val accessTokenService = identityResponse.serviceRemoteTicketing.firstOrNull {
                it.type == ServiceType.ACCESS_TOKEN_SERVICE.type
            } ?: throw AccessCredentialServiceEndpointNotFoundException()
            val validationServices = identityResponse.serviceRemoteTicketing.filter {
                it.type == ServiceType.VALIDATION_SERVICE.type
            }
            if (validationServices.isEmpty()) {
                throw AccessTokenRequestException(HttpStatusCode.NotFound)
            }
            return TicketingIdentityDocument(
                accessTokenService,
                validationServices
            )
        } catch (e: ClientRequestException) {
            throw IdentityDocumentRequestException(e.response.status)
        }
    }
}

// Error number 2
public class IdentityDocumentRequestException(public val code: HttpStatusCode) : Exception()

// Error number 3
public class AccessCredentialServiceEndpointNotFoundException : Exception()
