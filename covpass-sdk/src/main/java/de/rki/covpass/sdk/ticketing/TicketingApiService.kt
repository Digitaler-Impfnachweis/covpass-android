/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.ticketing.data.accesstoken.TicketingAccessTokenRequest
import de.rki.covpass.sdk.ticketing.data.identity.TicketingIdentityDocumentResponse
import de.rki.covpass.sdk.ticketing.data.identity.TicketingValidationServiceIdentityResponse
import de.rki.covpass.sdk.ticketing.data.validate.TicketingValidationRequest
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

public class TicketingApiService(httpClient: HttpClient) {
    private val client = httpClient.config {
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public suspend fun getIdentity(
        url: String,
    ): TicketingIdentityDocumentResponse = client.get(url)

    public suspend fun getAccessToken(
        url: String,
        authHeader: String,
        accessTokenRequest: TicketingAccessTokenRequest,
    ): HttpResponse = client.post(url) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Any)
        header(X_VERSION_HEADER, X_VERSION_VALUE)
        header(AUTHORIZATION_HEADER, authHeader.createAuthHeader())
        body = accessTokenRequest
    }

    public suspend fun getValidationServiceIdentity(
        url: String,
    ): TicketingValidationServiceIdentityResponse = client.get(url)

    public suspend fun validate(
        url: String,
        authHeader: String,
        ticketingValidationRequest: TicketingValidationRequest,
    ): HttpResponse = client.post(url) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Any)
        header(X_VERSION_HEADER, X_VERSION_VALUE)
        header(AUTHORIZATION_HEADER, authHeader.createAuthHeader())
        body = ticketingValidationRequest
    }

    public suspend fun cancelValidation(
        url: String,
        authHeader: String,
    ): HttpResponse = client.get(url) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Any)
        header(X_VERSION_HEADER, X_VERSION_VALUE)
        header(AUTHORIZATION_HEADER, authHeader.createAuthHeader())
    }

    private companion object {
        const val X_VERSION_HEADER = "X-Version"
        const val X_VERSION_VALUE = "1.0.0"
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
