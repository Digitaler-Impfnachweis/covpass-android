/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import com.ensody.reactivestate.test.CoroutineTest
import de.rki.covpass.sdk.ticketing.data.identity.TicketingValidationServiceIdentityResponse
import io.ktor.client.features.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ValidationServiceIdentityRepositoryTest : CoroutineTest() {

    @Test
    fun `test fetchValidationServiceIdentity`() = runTest {
        val expectedTicketingAccessTokenData = TicketingValidationServiceIdentityResponse(
            "id",
            emptyList(),
        )
        val ticketingApiService: TicketingApiService = mockk()

        coEvery {
            ticketingApiService.getValidationServiceIdentity(any())
        } returns TicketingValidationServiceIdentityResponse(
            "id",
            emptyList(),
        )

        val repository = ValidationServiceIdentityRepository(
            ticketingApiService,
        )
        val ticketingAccessTokenData = repository.fetchValidationServiceIdentity("")
        assertEquals(ticketingAccessTokenData, expectedTicketingAccessTokenData)
    }

    @Test
    fun `test fetchValidationServiceIdentity throws IdentityDocumentValidationRequestException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery {
            ticketingApiService.getValidationServiceIdentity(any())
        } throws ClientRequestException(httpResponse, "")

        val repository = ValidationServiceIdentityRepository(
            ticketingApiService,
        )
        assertFailsWith<IdentityDocumentValidationRequestException> {
            repository.fetchValidationServiceIdentity("")
        }
    }
}
