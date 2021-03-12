package com.ibm.health.common.http

import com.ibm.health.common.gson.defaultGsonBuilder
import com.ibm.health.common.http.retry.RetryInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.http.URLProtocol
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Interface for common HTTP client configuration.
 *
 * It is required for certification that you only use the client defined here.
 */
public interface HttpConfig {
    /** Activates request logging. You MUST NOT use this in release builds! */
    public fun enableLogging(logLevel: HttpLogLevel = HttpLogLevel.HEADERS)

    /** An `OkHttpClient` with correct TLS settings. */
    public val okHttpClient: OkHttpClient

    /** Creates a Ktor `HttpClient` with correct TLS settings and optionally with an additional config [block]. */
    public fun ktorClient(block: HttpClientConfig<OkHttpConfig>.() -> Unit = {}): HttpClient
}

/** The global [HttpConfig] instance. */
public var httpConfig: HttpConfig = DefaultHttpConfig()
    private set

internal fun resetHttpConfig() {
    httpConfig = DefaultHttpConfig()
}

private class DefaultHttpConfig : HttpConfig {
    private var frozen = false
    private var logging: HttpLogLevel = HttpLogLevel.NONE

    override fun enableLogging(logLevel: HttpLogLevel) {
        // This is meant as a security measure, so you don't mistakenly enable logging in release builds.
        // We want to enforce as much as possible that logging can only be enabled at app launch.
        if (frozen) {
            throw IllegalStateException("The HttpConfig is frozen already. Please enable logging only at app launch.")
        }
        frozen = true

        logging = logLevel
    }

    private val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.RESTRICTED_TLS)
        .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
        .cipherSuites(
            // TLS 1.3
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_AES_128_GCM_SHA256,

            // TLS 1.2
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
        )
        .build()

    private val trustManager: X509TrustManager by lazy {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers: $trustManagers")
        }
        CustomTrustManager(trustManagers[0] as X509TrustManager)
    }

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance("TLS").apply {
            init(null, arrayOf(trustManager), SecureRandom())
        }
    }

    private val sslSocketFactory: SSLSocketFactory by lazy {
        sslContext.socketFactory
    }

    override val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            frozen = true
            followRedirects(false)
            connectionSpecs(listOf(connectionSpec))
            sslSocketFactory(sslSocketFactory, trustManager)

            when (logging) {
                HttpLogLevel.NONE -> Unit
                HttpLogLevel.HEADERS ->
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)) as Any
                HttpLogLevel.BODY ->
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) as Any
            }.let {}
        }.build()
    }

    override fun ktorClient(block: HttpClientConfig<OkHttpConfig>.() -> Unit): HttpClient =
        HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient.newBuilder()
                    .connectTimeout(0, TimeUnit.MILLISECONDS)
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .writeTimeout(0, TimeUnit.MILLISECONDS)
                    .addInterceptor(RetryInterceptor())
                    .build()
            }
            followRedirects = false
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
            install(FixedJsonFeature) {
                serializer = GsonSerializer {
                    defaultGsonBuilder()
                }
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                }
            }
            block()
        }
}

/** Represents the amount of logging. */
public enum class HttpLogLevel {
    /** Nothing will be logged. This is the default and MUST be used for release builds! */
    NONE,

    /** Logs request and response lines with headers. This MUST NOT be used in release builds! */
    HEADERS,

    /** Logs whole request and response lines with headers and body. This MUST NOT be used in release builds! */
    @Deprecated("This can break streaming requests. Use this only temporarily.")
    BODY
}
