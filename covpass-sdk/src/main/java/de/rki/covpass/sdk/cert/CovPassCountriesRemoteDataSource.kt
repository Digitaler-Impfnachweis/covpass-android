/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.dependencies.defaultJson
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.host

public class CovPassCountriesRemoteDataSource(httpClient: HttpClient, host: String) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public suspend fun getCountries(): List<String> =
        client.get("countrylist")
}
