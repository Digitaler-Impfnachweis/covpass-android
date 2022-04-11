/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import io.ktor.client.features.*
import okhttp3.internal.http2.StreamResetException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

public fun isNetworkError(error: Throwable): Boolean =
    when (error) {
        is UnknownHostException,
        is StreamResetException,
        is SocketTimeoutException,
        is HttpRequestTimeoutException,
        is TimeoutException,
        is ProtocolException,
        is ResponseException -> {
            true
        }
        else ->
            false
    }
