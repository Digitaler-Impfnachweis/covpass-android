package com.ibm.health.common.http.retry

import io.ktor.client.request.HttpRequestBuilder

/**
 * if [HttpRequestBuilder.retry] is set to true then a header will be set to the request which tells
 * OkHttp to retry the request according to the implementation.
 * See [RetryInterceptor] for implementation details of the retry handling
 */
public var HttpRequestBuilder.retry: Boolean?
    get() = headers[RetryInterceptor.RETRY_ALLOWED_HEADER]?.let {
        it == RetryInterceptor.RETRY_ALLOWED_HEADER
    }
    set(value) {
        value?.let {
            headers[RetryInterceptor.RETRY_ALLOWED_HEADER] =
                if (value)
                    RetryInterceptor.RETRY_ALLOWED_VALUE
                else
                    RetryInterceptor.RETRY_DISABLED_VALUE
        } ?: headers.remove(RetryInterceptor.RETRY_ALLOWED_HEADER)
    }
