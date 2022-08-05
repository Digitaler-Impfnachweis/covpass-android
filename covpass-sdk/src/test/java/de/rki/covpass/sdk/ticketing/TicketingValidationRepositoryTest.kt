/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import com.ensody.reactivestate.test.CoroutineTest
import de.rki.covpass.sdk.ticketing.data.validate.BookingPortalValidationResponseResult
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.ticketing.data.validate.TicketingValidationRequest
import io.ktor.client.call.receive
import io.ktor.client.features.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TicketingValidationRepositoryTest : CoroutineTest() {

    @Test
    fun `test fetchValidationResult throws TicketingSendingCertificateException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery {
            ticketingApiService.validate(any(), any(), any())
        } throws ClientRequestException(httpResponse, "")

        val repository = TicketingValidationRepository(
            ticketingApiService,
        )
        assertFailsWith<TicketingSendingCertificateException> {
            repository.fetchValidationResult(
                "",
                "",
                TicketingValidationRequest("", "", "", "", "", ""),
            )
        }
    }

    @Test
    fun `test fetchValidationResult`() = runTest {
        val expectedBookingValidationResponse = BookingValidationResponse(
            BookingPortalValidationResponseResult.OK,
            "",
            "",
            1,
            listOf(""),
            "",
            1,
            listOf(),
        )
        val encodedBookingValidationResponse = "eyJyZXN1bHQiOiJPSyIsInN1YiI6IiIsImlzcyI6IiIsImV4cCI6MSwiY2F0ZWdvcnkiO" +
            "lsiIl0sImNvbmZpcm1hdGlvbiI6IiIsImlhdCI6MSwicmVzdWx0cyI6W119"
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk()

        coEvery {
            ticketingApiService.validate(any(), any(), any())
        } returns httpResponse
        coEvery {
            httpResponse.receive<String>()
        } returns "header.$encodedBookingValidationResponse"

        val repository = TicketingValidationRepository(
            ticketingApiService,
        )
        val bookingValidationResponse = repository.fetchValidationResult(
            "",
            "",
            TicketingValidationRequest("", "", "", "", "", ""),
        )
        assertEquals(bookingValidationResponse, expectedBookingValidationResponse)
    }
}
