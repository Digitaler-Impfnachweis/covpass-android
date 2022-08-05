/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import com.ensody.reactivestate.test.CoroutineTest
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenData
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenRequest
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AccessTokenRepositoryTest : CoroutineTest() {

    @Test
    fun `test fetchAccessToken throws AccessTokenRequestException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery {
            ticketingApiService.getAccessToken(any(), any(), any())
        } throws ClientRequestException(httpResponse, "")

        val repository = AccessTokenRepository(
            ticketingApiService
        )
        assertFailsWith<AccessTokenRequestException> {
            repository.fetchAccessToken("", "", TicketingAccessTokenRequest("", ""))
        }
    }

    @Test
    fun `test fetchAccessToken`() = runTest {

        val expectedTicketingAccessTokenData = TicketingAccessTokenData(
            "jwtToken", "iv"
        )
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery {
            ticketingApiService.getAccessToken(any(), any(), any())
        } returns httpResponse

        coEvery { httpResponse.receive<String>() } returns "jwtToken"
        coEvery { httpResponse.headers["x-nonce"] } returns "iv"

        val repository = AccessTokenRepository(
            ticketingApiService
        )
        val ticketingAccessTokenData: TicketingAccessTokenData =
            repository.fetchAccessToken("", "", TicketingAccessTokenRequest("", ""))
        assertEquals(ticketingAccessTokenData, expectedTicketingAccessTokenData)
    }

    @Test
    fun `test fetchAccessToken null headers x-nonce`() = runTest {

        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery {
            ticketingApiService.getAccessToken(any(), any(), any())
        } returns httpResponse

        coEvery { httpResponse.receive<String>() } returns "jwtToken"
        coEvery { httpResponse.headers["x-nonce"] } returns null

        val repository = AccessTokenRepository(
            ticketingApiService
        )
        assertFailsWith<IllegalStateException> {
            repository.fetchAccessToken("", "", TicketingAccessTokenRequest("", ""))
        }
    }
}
