/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.rules.remote.rules.domestic.CovPassDomesticRuleIdentifierRemote
import de.rki.covpass.sdk.rules.remote.rules.eu.CovPassRuleRemote
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.host

public class CovPassDomesticRulesRemoteDataSource(httpClient: HttpClient, host: String) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public suspend fun getRuleIdentifiers(): List<CovPassDomesticRuleIdentifierRemote> =
        client.get("domesticrules")

    public suspend fun getRule(hash: String): CovPassRuleRemote =
        client.get("domesticrules/$hash")
}
