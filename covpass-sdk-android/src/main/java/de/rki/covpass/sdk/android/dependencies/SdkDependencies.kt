/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.dependencies

import android.app.Application
import de.rki.covpass.http.httpConfig
import de.rki.covpass.http.pinPublicKey
import de.rki.covpass.sdk.android.R
import de.rki.covpass.sdk.android.cert.*
import de.rki.covpass.sdk.android.cert.models.DscList
import de.rki.covpass.sdk.android.crypto.readPemAsset
import de.rki.covpass.sdk.android.crypto.readPemKeyAsset
import de.rki.covpass.sdk.android.utils.readTextAsset

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
 * Access to various dependencies for covpass-sdk-android module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    public open val trustServiceHost: String by lazy {
        application.getString(R.string.trust_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("You have to set @string/trust_service_host or override trustServiceHost")
    }

    private val httpClient by lazy { httpConfig.ktorClient() }

    private val backendCa by lazy { application.readPemAsset("covpass-sdk-android/backend-ca.pem") }

    public val dscList: DscList by lazy {
        decoder.decodeDscList(
            application.readTextAsset("covpass-sdk-android/dsc-list.json")
        )
    }

    public val dscListService: DscListService by lazy { DscListService(httpClient, trustServiceHost) }

    public val validator: CertValidator by lazy { CertValidator(dscList.toTrustedCerts()) }

    public val decoder: DscListDecoder by lazy { DscListDecoder(publicKey.first()) }

    private val publicKey by lazy { application.readPemKeyAsset("covpass-sdk-android/dsc-list-signing-key.pem") }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    internal fun init() {
        httpConfig.pinPublicKey(backendCa)
    }
}
