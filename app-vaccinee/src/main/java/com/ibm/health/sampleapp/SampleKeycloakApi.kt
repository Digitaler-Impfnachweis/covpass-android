package com.ibm.health.sampleapp

import com.ibm.health.common.http.httpConfig
import io.ktor.client.features.cookies.AcceptAllCookiesStorage
import io.ktor.client.features.cookies.HttpCookies
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.http.takeFrom
import com.ibm.health.common.logging.Lumber
import java.io.IOException

class SampleKeycloakApi(identityProviderUrl: String) {
    private val client = httpConfig.ktorClient()
    private val redirectUrl = identityProviderUrl + "broker/sample-insurer/endpoint"
    val authUrl = URLBuilder().apply {
        takeFrom(identityProviderUrl)
        path("auth/realms/ega-sample/protocol/openid-connect/auth")
        parameters.apply {
            append("response_type", "code")
            append("client_id", "ega-sample-client")
            append("redirect_uri", redirectUrl)
            append("scope", "openid")
            append("nonce", "88fb817d2bec28f534493087058d19ef")
        }
    }.buildString()

    /** Logs user in using [username] and [password] and returns the auth code or a [LoginError]. */
    suspend fun login(username: String, password: String): String {
        // We need to keep track of cookies and the final redirect will contain the auth code
        val client = client.config {
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            followRedirects = false
            expectSuccess = false
        }
        // Here we interact with an HTML-based form
        var body = ""
        try {
            body = client.get(authUrl)
        } catch (e: IOException) {
            // Ignore exception here, will lead to LoginError later. Else detekt complains about throws count.
            Lumber.e(e)
        }
        val match = actionRegex.find(body)
        if (match == null || match.groupValues.size <= 1) {
            throw LoginError()
        }
        // TODO: handle full HTML unescaping
        val action = match.groupValues[1].replace("&amp;", "&")
        // POST the login details
        val response: HttpResponse = client.submitForm(
            action,
            Parameters.build {
                append("username", username)
                append("password", password)
            }
        )
        return response.headers["Location"]?.let { url ->
            try {
                Url(url).parameters["code"]
            } catch (e: URLParserException) {
                null
            }
        } ?: throw LoginError()
    }

    companion object {
        // Extracts a form action from the HTML body
        private val actionRegex = Regex(
            """<form[^>]+action=["'](https://[^"']*)["']""",
            RegexOption.MULTILINE
        )
    }
}

class LoginError : RuntimeException()
