/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.cert

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*

public class DscListService(httpClient: HttpClient, host: String) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
    }

    public suspend fun getTrustedList(): String =
        client.get("/trustList/DSC/DE/")
}
