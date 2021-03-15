package com.ibm.health.common.http.retry

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Adding this interceptor will cause OkHttp to retry
 * any GET, HEAD and OPTIONS request unless a Header
 * "X-Retry-Allowed" with the value "disabled" is set
 * (see [RetryInterceptor.RETRY_ALLOWED_HEADER] and [RetryInterceptor.RETRY_DISABLED_VALUE]).
 * If no header is set then by default the retry will be performed.
 * The header will be removed before the request is processed, therefore
 * it will not be transmitted
 */
internal class RetryInterceptor : Interceptor {
    private val allowedRetryMethods = listOf("GET", "HEAD", "OPTIONS")

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val httpMethod = request.method
        var attempt = 0
        val retryEnabled = isRetryEnabled(request)

        // If a retry header is set then it must be removed since it's only for internal use
        if (request.header(RETRY_ALLOWED_HEADER) != null) {
            request = request
                .newBuilder()
                .removeHeader(RETRY_ALLOWED_HEADER)
                .build()
        }

        while (true) {
            try {
                attempt += 1
                val response = chain.proceed(request)
                if (shouldRetry(httpMethod, response.code, attempt, retryEnabled)) {
                    Thread.sleep(RETRY_DELAY_IN_MS)
                    continue
                }
                return response
            } catch (exception: Throwable) {
                if (!shouldRetry(httpMethod, null, attempt, retryEnabled)) {
                    // okHttp only sends IOExceptions over thread boundaries
                    throw exception as? IOException ?: IOException(exception)
                }
                Thread.sleep(RETRY_DELAY_IN_MS)
            }
        }
    }

    private fun shouldRetry(
        httpMethod: String,
        statusCode: Int?,
        retries: Int,
        retryEnabled: Boolean
    ): Boolean {
        return retries < MAX_ATTEMPTS &&
            retryEnabled &&
            allowedRetryMethods.contains(httpMethod) &&
            (statusCode == null || statusCode in 500..599)
    }

    private fun isRetryEnabled(request: Request): Boolean {
        val retryHeader = request.headers[RETRY_ALLOWED_HEADER]
        return retryHeader == null || retryHeader == RETRY_ALLOWED_VALUE
    }

    companion object {
        internal const val MAX_ATTEMPTS = 7
        private const val RETRY_DELAY_IN_MS = 500L
        const val RETRY_ALLOWED_HEADER = "X-Retry-Allowed-6jNGI0d8vBVJg6X9YwUG"
        const val RETRY_ALLOWED_VALUE = "allowed"
        const val RETRY_DISABLED_VALUE = "disabled"
    }
}
