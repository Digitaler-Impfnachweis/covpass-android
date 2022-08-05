/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.DscList
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.ResponseException
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.etag

/**
 * Service to fetch the Document Signer Certificates from backend.
 */
public class DscListService(
    httpClient: HttpClient,
    host: String,
    private val dscListDecoder: DscListDecoder,
) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
    }

    /**
     * Fetch the Document Signer Certificates from backend.
     */
    public suspend fun getTrustedList(dscList: DscList? = null): DscList =
        try {
            val httpResponse: HttpResponse = client.get("/trustList/DSC/") {
                dscList?.etag?.let {
                    if (it.isNotEmpty()) {
                        header("If-None-Match", it)
                    }
                }
            }
            dscListDecoder.decodeDscList(
                httpResponse.receive(),
            ).copy(etag = httpResponse.etag() ?: "")
        } catch (e: ResponseException) {
            if (dscList == null || e.response.status != HttpStatusCode.NotModified) {
                throw e
            }
            dscList
        }
}
