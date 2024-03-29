/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http.util

import io.ktor.client.HttpClientConfig
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpResponseValidator
import io.ktor.client.features.ResponseException
import io.ktor.client.features.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.Url

/** Only throws exceptions on >= 400, but not on redirects. */
public fun HttpClientConfig<*>.addErrorValidation() {
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            if (statusCode < 400) {
                return@validateResponse
            }

            val body = response.readText()
            when (statusCode) {
                in 400..499 -> throw ClientRequestException(response, body)
                in 500..599 -> throw ServerResponseException(response, body)
                else -> throw ResponseException(response, body)
            }
        }
    }
}

/** Retrieves the redirect URL or throws an exception. */
public val HttpResponse.redirectUrl: Url
    get() = requireNotNull(redirectUrlOrNull)

/** Retrieves the redirect URL or null. */
public val HttpResponse.redirectUrlOrNull: Url?
    get() = if (!isRedirect) null else headers["Location"]?.let { parseUrl(it) }

/** Whether the response status indicates this is a redirect. */
public val HttpResponse.isRedirect: Boolean
    get() = status.value in 300..399
