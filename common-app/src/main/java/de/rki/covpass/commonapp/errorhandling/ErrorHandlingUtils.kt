/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.errorhandling

import io.ktor.client.features.*
import okhttp3.internal.http2.StreamResetException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

public fun isConnectionError(error: Throwable): Boolean =
    when {
        isNoInternetError(error) -> true
        error is ProtocolException -> true
        error is ResponseException ->
            // If the http code is 4xx, a ClientRequestException is thrown.
            // This case shall be handled as standard error, not connection error.
            error !is ClientRequestException
        else ->
            false
    }

public fun isNoInternetError(error: Throwable): Boolean =
    when (error) {
        is UnknownHostException,
        is StreamResetException,
        is SocketTimeoutException,
        is HttpRequestTimeoutException,
        is TimeoutException,
        -> true
        else -> false
    }
