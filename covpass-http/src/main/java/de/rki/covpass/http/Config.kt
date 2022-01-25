/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.http

import de.rki.covpass.http.retry.RetryInterceptor
import de.rki.covpass.logging.Lumber
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.*
import io.ktor.http.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Interface for common HTTP client configuration.
 */
public interface HttpConfig {
    /** Activates request logging. You MUST NOT use this in release builds! */
    public fun enableLogging(logLevel: HttpLogLevel = HttpLogLevel.HEADERS)

    /** Enables public key pinning for the given [pattern] and [pin] expression (e.g. sha256/...). */
    public fun pinPublicKey(pattern: String, pin: String)

    /** An `OkHttpClient` with correct TLS settings. */
    public val okHttpClient: OkHttpClient

    /** Creates a Ktor `HttpClient` with correct TLS settings and optionally with an additional config [block]. */
    public fun ktorClient(block: HttpClientConfig<OkHttpConfig>.() -> Unit = {}): HttpClient

    /** Returns true if the provided URL's hostname public key is pinned */
    public fun hasPublicKey(url: String): Boolean

    /** Sets UserAgent for the Ktor client */
    public fun setUserAgent(userAgent: String)
}

/**
 * Enables public key pinning for the given list of [X509Certificates][X509Certificate] and the hosts defined within.
 */
public fun HttpConfig.pinPublicKey(certs: List<X509Certificate>) {
    for (cert in certs) {
        // Check if this is a DNS SAN (code 2)
        cert.getDnsSubjectAlternativeNames().forEach {
            pinPublicKey(it, cert)
        }
    }
}

/** Enables public key pinning for the given [pattern] using a list of [X509Certificates][X509Certificate]. */
public fun HttpConfig.pinPublicKey(pattern: String, certs: List<X509Certificate>) {
    for (cert in certs) {
        pinPublicKey(pattern, cert)
    }
}

/** Enables public key pinning for the given [pattern] using a list of [X509Certificates][X509Certificate]. */
public fun HttpConfig.pinPublicKey(pattern: String, cert: X509Certificate) {
    pinPublicKey(pattern, CertificatePinner.pin(cert))
}

/**
 * Extract the SAN from [X509Certificate]
 * @return [Set] which contains SubjectAlternativeNames as Strings
 */
public fun X509Certificate.getDnsSubjectAlternativeNames(): Set<String> {
    return subjectAlternativeNames.filter { san ->
        san.size >= 2 && san[0] == 2 && san[1] is String
    }.mapNotNull { it[1] as String }.toSet()
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
    private var userAgent: String? = null

    private fun checkFrozen() {
        // This is meant as a security measure, so you don't mistakenly enable logging or change configs after the fact.
        if (frozen) {
            throw IllegalStateException("The HttpConfig is frozen already. Please enable logging only at app launch.")
        }
    }

    override fun enableLogging(logLevel: HttpLogLevel) {
        // We want to enforce as much as possible that logging can only be enabled at app launch.
        checkFrozen()
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
            "Unexpected default trust managers: $trustManagers"
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

    private val certPinnerBuilder = CertificatePinner.Builder()

    override fun pinPublicKey(pattern: String, pin: String) {
        checkFrozen()
        Lumber.d { "Pinning host pattern $pattern to public key $pin" }
        certPinnerBuilder.add(pattern, pin)
    }

    override val okHttpClient: OkHttpClient by lazy {
        frozen = true
        OkHttpClient.Builder().apply {
            followRedirects(false)
            connectionSpecs(listOf(connectionSpec))
            certificatePinner(certPinnerBuilder.build())
            sslSocketFactory(sslSocketFactory, trustManager)

            @Suppress("IMPLICIT_CAST_TO_ANY", "DEPRECATION")
            when (logging) {
                HttpLogLevel.NONE -> Unit
                HttpLogLevel.HEADERS ->
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                HttpLogLevel.BODY ->
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
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
            userAgent?.let {
                install(UserAgent) {
                    agent = it
                }
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                }
            }
            block()
        }

    override fun hasPublicKey(url: String): Boolean {
        val hostname = Url(url).host
        return okHttpClient.certificatePinner.findMatchingPins(hostname).isNotEmpty()
    }

    override fun setUserAgent(userAgent: String) {
        this.userAgent = userAgent
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
