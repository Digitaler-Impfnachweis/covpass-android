package com.ibm.health.vaccination.sdk.android.dependencies

import android.app.Application
import com.ibm.health.common.http.httpConfig
import com.ibm.health.vaccination.sdk.android.cert.CertService
import com.ibm.health.vaccination.sdk.android.cert.QRCoder
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.readPem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * Global var for making the [SdkDependencies] accessible.
 */
public lateinit var sdkDeps: SdkDependencies

/**
 * Access to various dependencies for sdk-android module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    // FIXME: This should by default be the production host
    /** The host against which the vaccination -> validation cert exchange is done. */
    public open val certServiceHost: String = "api.recertify.demo.ubirch.com"

    internal val httpClient = httpConfig.ktorClient()

    private val ca by lazy {
        readPem(application.assets.open("default-ca.pem").bufferedReader().use { it.readText() })
    }

    private val validator by lazy { CertValidator(ca) }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    public val certService: CertService by lazy { CertService(httpClient, certServiceHost, qrCoder) }

    public val mainScope: CoroutineScope by lazy { MainScope() }
}
