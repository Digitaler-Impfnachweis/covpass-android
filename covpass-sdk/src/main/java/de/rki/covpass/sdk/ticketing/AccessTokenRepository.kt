/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenData
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenRequest
import io.ktor.client.call.receive
import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpStatusCode

public class AccessTokenRepository(
    public val ticketingApiService: TicketingApiService,
) {
    public suspend fun fetchAccessToken(
        url: String,
        header: String,
        ticketingAccessTokenRequest: TicketingAccessTokenRequest,
    ): TicketingAccessTokenData {
        try {
            val response = ticketingApiService.getAccessToken(url, header, ticketingAccessTokenRequest)
            return TicketingAccessTokenData(
                jwtToken = response.receive(),
                iv = response.headers["x-nonce"] ?: throw IllegalStateException(),
            )
        } catch (exception: ClientRequestException) {
            throw AccessTokenRequestException(exception.response.status)
        }
    }
}

// Error number 8
public class AccessTokenRequestException(public val code: HttpStatusCode) : Exception()
