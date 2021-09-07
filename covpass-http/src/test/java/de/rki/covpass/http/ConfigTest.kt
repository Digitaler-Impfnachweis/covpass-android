/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http

import okhttp3.logging.HttpLoggingInterceptor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ConfigTest {
    @BeforeTest
    fun setUp() {
        resetHttpConfig()
    }

    @Test
    fun `logging cannot be enabled after first access`() {
        httpConfig.ktorClient()
        assertFailsWith<IllegalStateException> {
            httpConfig.enableLogging(HttpLogLevel.BODY)
        }
    }

    @Test
    fun `logging disabled by default`() {
        assertTrue(httpConfig.okHttpClient.interceptors.none { it is HttpLoggingInterceptor })
    }

    @Test
    fun `enabling logging`() {
        httpConfig.enableLogging(HttpLogLevel.HEADERS)
        assertTrue(httpConfig.okHttpClient.interceptors.any { it is HttpLoggingInterceptor })
    }
}
