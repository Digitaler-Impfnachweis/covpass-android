/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.reissuing

import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.reissuing.remote.CertificateReissueRequest
import de.rki.covpass.sdk.reissuing.remote.CertificateReissueResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

public class ReissuingApiService(
    httpClient: HttpClient,
    host: String
) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public suspend fun reissueCertificate(
        action: String,
        certificates: List<String>
    ): List<CertificateReissueResponse> =
        try {
            val httpResponse: HttpResponse = client.post<HttpResponse>("/api/certify/v2/reissue") {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                body = CertificateReissueRequest(action, certificates)
            }.receive()
            httpResponse.receive()
        } catch (e: ResponseException) {
            when (e.response.status) {
                HttpStatusCode.TooManyRequests -> throw ReissuingTooManyRequests()
                HttpStatusCode.InternalServerError -> throw ReissuingInternalServerError()
                else -> throw e
            }
        }
}

public class ReissuingTooManyRequests : IllegalStateException()

public class ReissuingInternalServerError : IllegalStateException()
