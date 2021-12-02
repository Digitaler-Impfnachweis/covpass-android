/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import io.ktor.client.call.*

public class CancellationRepository(
    public val ticketingApiService: TicketingApiService,
) {
    public suspend fun cancelTicketing(
        url: String,
        header: String,
    ): String {
        val response = ticketingApiService.cancelValidation(url, header)
        return response.receive()
    }
}
