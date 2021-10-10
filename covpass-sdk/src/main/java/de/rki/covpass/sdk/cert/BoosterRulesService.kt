/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.rules.booster.remote.BoosterRuleIdentifierRemote
import de.rki.covpass.sdk.rules.booster.remote.BoosterRuleRemote
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

public class BoosterRulesService(httpClient: HttpClient, host: String) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(defaultJson)
        }
    }

    public suspend fun getBoosterRuleIdentifiers(): List<BoosterRuleIdentifierRemote> =
        client.get("bnrules")

    public suspend fun getBoosterRule(hash: String): BoosterRuleRemote =
        client.get("bnrules/$hash")
}
