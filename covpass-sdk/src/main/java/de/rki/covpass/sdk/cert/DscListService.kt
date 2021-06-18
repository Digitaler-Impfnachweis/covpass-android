/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*

/**
 * Service to fetch the Document Signer Certificates from backend.
 */
public class DscListService(httpClient: HttpClient, host: String) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
    }

    /**
     * Fetch the Document Signer Certificates from backend.
     */
    public suspend fun getTrustedList(): String =
        client.get("/trustList/DSC/")
}
