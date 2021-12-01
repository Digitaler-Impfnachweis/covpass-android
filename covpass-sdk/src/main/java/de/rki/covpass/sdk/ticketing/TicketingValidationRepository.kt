/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.ticketing.data.validate.TicketingValidationRequest
import io.ktor.client.call.*
import io.ktor.client.features.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

public class TicketingValidationRepository(
    public val ticketingApiService: TicketingApiService,
) {
    public suspend fun fetchValidationResult(
        url: String,
        authHeader: String,
        ticketingValidationRequest: TicketingValidationRequest,
    ): BookingValidationResponse {
        try {
            val response = ticketingApiService.validate(url, authHeader, ticketingValidationRequest)
            return response.receive<String>().parseJwtToken().let {
                defaultJson.decodeFromString(it.body)
            }
        } catch (e: ClientRequestException) {
            throw TicketingSendingCertificateException()
        } catch (e: SerializationException) {
            throw TicketingCertificatePreparationException()
        }
    }
}

// Error number 11
public class TicketingCertificatePreparationException : Exception()

// Error number 12
public class TicketingSendingCertificateException : Exception()
