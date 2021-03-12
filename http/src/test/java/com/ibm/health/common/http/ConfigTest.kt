package com.ibm.health.common.http

import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.none
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test

internal class ConfigTest {
    @Before
    fun setUp() {
        resetHttpConfig()
    }

    @Test
    fun `logging cannot be enabled after first access`() {
        httpConfig.ktorClient()
        assertThat {
            httpConfig.enableLogging(HttpLogLevel.BODY)
        }.isFailure().isInstanceOf(IllegalStateException::class)
    }

    @Test
    fun `logging disabled by default`() {
        assertThat(httpConfig.okHttpClient.interceptors()).none { it.isInstanceOf(HttpLoggingInterceptor::class) }
    }

    @Test
    fun `enabling logging`() {
        httpConfig.enableLogging(HttpLogLevel.HEADERS)
        assertThat(httpConfig.okHttpClient.interceptors()).any { it.isInstanceOf(HttpLoggingInterceptor::class) }
    }
}
