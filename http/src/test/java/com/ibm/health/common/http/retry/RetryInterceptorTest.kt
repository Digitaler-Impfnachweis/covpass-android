package com.ibm.health.common.http.retry

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import com.ibm.health.common.http.retry.RetryInterceptor.Companion.RETRY_ALLOWED_HEADER
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

internal class RetryInterceptorTest {
    private val subject = RetryInterceptor()

    @Test
    fun `should call proceed on request only once and complete successfully`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        every { request.header(RETRY_ALLOWED_HEADER) } returns null
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        every { request.method } returns "GET"
        every { request.headers } returns headersOf("a", "b")
        every { response.code } returns 200

        val result = subject.intercept(chain)

        assertThat(result).isEqualTo(response)
        verify(exactly = 1) { chain.proceed(request) }
    }

    @Test
    fun `should retry due to chain proceed exception`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        every { request.header(RETRY_ALLOWED_HEADER) } returns null
        every { chain.request() } returns request
        every { chain.proceed(request) } throws Exception()
        every { request.method } returns "GET"
        every { request.headers } returns headersOf("a", "b")
        every { response.code } returns 200

        assertThat {
            subject.intercept(chain)
        }.isFailure()

        verify(exactly = RetryInterceptor.MAX_ATTEMPTS) { chain.proceed(request) }
    }

    @Test
    fun `should retry due to status 500`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        every { request.header(RETRY_ALLOWED_HEADER) } returns null
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        every { request.method } returns "GET"
        every { request.headers } returns headersOf("a", "b")
        every { response.code } returns 500

        assertThat(subject.intercept(chain).code).isEqualTo(500)

        verify(exactly = RetryInterceptor.MAX_ATTEMPTS) { chain.proceed(request) }
    }

    @Test
    fun `should not retry due if status 500 but method is POST`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        every { request.header(RETRY_ALLOWED_HEADER) } returns null
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        every { request.method } returns "POST"
        every { request.headers } returns headersOf("a", "b")
        every { response.code } returns 500

        subject.intercept(chain)

        verify(exactly = 1) { chain.proceed(request) }
    }

    @Test
    fun `should retry only once if status 500 and retry is explicitly not allowed through the header`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        val builder: Request.Builder = mockk()
        // mock the builder
        every { request.newBuilder() } returns builder
        every { builder.removeHeader(RETRY_ALLOWED_HEADER) } returns builder
        every { builder.build() } returns request
        every { request.header(RETRY_ALLOWED_HEADER) } returns "bla"
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        every { request.method } returns "GET"
        every { request.headers } returns mutableMapOf(RETRY_ALLOWED_HEADER to RetryInterceptor.RETRY_DISABLED_VALUE)
            .toHeaders()
        every { response.code } returns 500

        subject.intercept(chain)

        verify(exactly = 1) { chain.proceed(request) }
        verify(exactly = 1) { builder.removeHeader(RETRY_ALLOWED_HEADER) }
        verify(exactly = 1) { builder.build() }
    }

    @Test
    fun `should retry due if status 500 and retry is allowed through the header`() {
        val chain: Interceptor.Chain = mockk()
        val request: Request = mockk()
        val response: Response = mockk()
        val builder: Request.Builder = mockk()
        // mock the builder
        every { request.newBuilder() } returns builder
        every { builder.removeHeader(RETRY_ALLOWED_HEADER) } returns builder
        every { builder.build() } returns request
        every { request.header(RETRY_ALLOWED_HEADER) } returns "bla"
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        every { request.method } returns "GET"
        every { request.headers } returns headersOf(RETRY_ALLOWED_HEADER,
            RetryInterceptor.RETRY_ALLOWED_VALUE
        )
        every { response.code } returns 500

        assertThat(subject.intercept(chain).code).isEqualTo(500)

        verify(exactly = RetryInterceptor.MAX_ATTEMPTS) { chain.proceed(request) }
        verify(exactly = 1) { builder.removeHeader(RETRY_ALLOWED_HEADER) }
        verify(exactly = 1) { builder.build() }
    }
}
