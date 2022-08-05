/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import com.ensody.reactivestate.test.CoroutineTest
import de.rki.covpass.sdk.ticketing.data.identity.ServiceType
import de.rki.covpass.sdk.ticketing.data.identity.TicketingIdentityDocument
import de.rki.covpass.sdk.ticketing.data.identity.TicketingIdentityDocumentResponse
import de.rki.covpass.sdk.ticketing.data.identity.TicketingServiceRemote
import io.ktor.client.features.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class IdentityDocumentRepositoryTest : CoroutineTest() {

    @Test
    fun `test fetchIdentityDocument`() = runTest {
        val expectedTicketingIdentityDocument = TicketingIdentityDocument(
            TicketingServiceRemote(
                "",
                ServiceType.ACCESS_TOKEN_SERVICE.type,
                "",
                "",
            ),
            listOf(
                TicketingServiceRemote(
                    "",
                    ServiceType.VALIDATION_SERVICE.type,
                    "",
                    "",
                ),
            ),
            TicketingServiceRemote(
                "",
                ServiceType.CANCELLATION_SERVICE.type,
                "",
                "",
            ),
        )
        val ticketingApiService: TicketingApiService = mockk()

        coEvery { ticketingApiService.getIdentity(any()) } returns TicketingIdentityDocumentResponse(
            "id",
            emptyList(),
            listOf(
                TicketingServiceRemote(
                    "",
                    ServiceType.ACCESS_TOKEN_SERVICE.type,
                    "",
                    "",
                ),
                TicketingServiceRemote(
                    "",
                    ServiceType.VALIDATION_SERVICE.type,
                    "",
                    "",
                ),
                TicketingServiceRemote(
                    "",
                    ServiceType.CANCELLATION_SERVICE.type,
                    "",
                    "",
                ),
            ),
        )

        val repository = IdentityDocumentRepository(
            ticketingApiService,
        )
        val ticketingIdentityDocument = repository.fetchIdentityDocument(
            TicketingDataInitialization(
                protocol = "",
                protocolVersion = "",
                serviceIdentity = "",
                privacyUrl = "",
                token = "",
                consent = "",
                subject = "",
                serviceProvider = "",
            ),
        )
        assertEquals(ticketingIdentityDocument, expectedTicketingIdentityDocument)
    }

    @Test
    fun `test fetchIdentityDocument throws AccessTokenRequestException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()

        coEvery { ticketingApiService.getIdentity(any()) } returns TicketingIdentityDocumentResponse(
            "id",
            emptyList(),
            listOf(
                TicketingServiceRemote(
                    "",
                    ServiceType.ACCESS_TOKEN_SERVICE.type,
                    "",
                    "",
                ),
                TicketingServiceRemote(
                    "",
                    ServiceType.CANCELLATION_SERVICE.type,
                    "",
                    "",
                ),
            ),
        )

        val repository = IdentityDocumentRepository(
            ticketingApiService,
        )
        assertFailsWith<AccessTokenRequestException> {
            repository.fetchIdentityDocument(
                TicketingDataInitialization(
                    protocol = "",
                    protocolVersion = "",
                    serviceIdentity = "",
                    privacyUrl = "",
                    token = "",
                    consent = "",
                    subject = "",
                    serviceProvider = "",
                ),
            )
        }
    }

    @Test
    fun `test fetchIdentityDocument throws ClientRequestException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()
        val httpResponse: HttpResponse = mockk(relaxed = true)

        coEvery { ticketingApiService.getIdentity(any()) } throws ClientRequestException(httpResponse, "")

        val repository = IdentityDocumentRepository(
            ticketingApiService,
        )
        assertFailsWith<IdentityDocumentRequestException> {
            repository.fetchIdentityDocument(
                TicketingDataInitialization(
                    protocol = "",
                    protocolVersion = "",
                    serviceIdentity = "",
                    privacyUrl = "",
                    token = "",
                    consent = "",
                    subject = "",
                    serviceProvider = "",
                ),
            )
        }
    }

    @Test
    fun `test fetchIdentityDocument throws IllegalArgumentException`() = runTest {
        val ticketingApiService: TicketingApiService = mockk()

        coEvery { ticketingApiService.getIdentity(any()) } throws IllegalArgumentException("")

        val repository = IdentityDocumentRepository(
            ticketingApiService,
        )
        assertFailsWith<IdentityDocumentRequestException> {
            repository.fetchIdentityDocument(
                TicketingDataInitialization(
                    protocol = "",
                    protocolVersion = "",
                    serviceIdentity = "",
                    privacyUrl = "",
                    token = "",
                    consent = "",
                    subject = "",
                    serviceProvider = "",
                ),
            )
        }
    }
}
