package com.ibm.health.vaccination.sdk.android.dependencies

import android.app.Application
import com.ibm.health.common.http.httpConfig
import com.ibm.health.common.http.pinPublicKey
import com.ibm.health.vaccination.sdk.android.cert.CertService
import com.ibm.health.vaccination.sdk.android.cert.QRCoder
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.readPemAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

/**
 * Global var for making the [SdkDependencies] accessible.
 */
private lateinit var _sdkDeps: SdkDependencies
public var sdkDeps: SdkDependencies
    get() = _sdkDeps
    set(value) {
        _sdkDeps = value
        value.init()
    }

/**
 * Access to various dependencies for sdk-android module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    /** Whether to use the production backend and DSC list. */
    public open val productionMode: Boolean = true

    /** The host against which the vaccination -> validation cert exchange is done. */
    public open val certServiceHost: String by lazy {
        if (productionMode) "api.recertify.ubirch.com"
        else "api.recertify.demo.ubirch.com"
    }

    internal val httpClient by lazy { httpConfig.ktorClient() }

    private val backendCa by lazy { application.readPemAsset("backend-ca.pem") }

    private val dscList by lazy {
        application.readPemAsset(
            if (productionMode) "dsc-list-production.pem" else "dsc-list-demo.pem"
        )
    }

    private val validator by lazy { CertValidator(dscList) }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    public val certService: CertService by lazy { CertService(httpClient, certServiceHost, qrCoder) }

    public val mainScope: CoroutineScope by lazy { MainScope() }

    internal fun init() {
        httpConfig.pinPublicKey(backendCa)
    }
}
