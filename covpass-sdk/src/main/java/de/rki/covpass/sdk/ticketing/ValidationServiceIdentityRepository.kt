/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.ticketing.data.identity.TicketingValidationServiceIdentityResponse
import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode

public class ValidationServiceIdentityRepository(
    public val ticketingApiService: TicketingApiService,
) {
    public suspend fun fetchValidationServiceIdentity(
        url: String,
    ): TicketingValidationServiceIdentityResponse {
        try {
            return ticketingApiService.getValidationServiceIdentity(url)
        } catch (e: ClientRequestException) {
            throw IdentityDocumentValidationRequestException(e.response.status)
        }
    }
}

// Error number 6
public class IdentityDocumentValidationRequestException(public val code: HttpStatusCode) : Exception()
