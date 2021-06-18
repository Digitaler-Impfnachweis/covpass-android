/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.dependencies

import android.app.Application
import de.rki.covpass.http.httpConfig
import de.rki.covpass.http.pinPublicKey
import de.rki.covpass.sdk.R
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.crypto.readPemAsset
import de.rki.covpass.sdk.crypto.readPemKeyAsset
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.utils.readTextAsset
import kotlinx.serialization.cbor.Cbor

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
 * Access to various dependencies for covpass-sdk module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    public open val trustServiceHost: String by lazy {
        application.getString(R.string.trust_service_host).takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("You have to set @string/trust_service_host or override trustServiceHost")
    }

    private val httpClient by lazy { httpConfig.ktorClient() }

    private val backendCa by lazy { application.readPemAsset("covpass-sdk/backend-ca.pem") }

    public val dscList: DscList by lazy {
        decoder.decodeDscList(
            application.readTextAsset("covpass-sdk/dsc-list.json")
        )
    }

    public val dscListService: DscListService by lazy { DscListService(httpClient, trustServiceHost) }

    public val dscRepository: DscRepository by lazy {
        DscRepository(CborSharedPrefsStore("dsc_cert_prefs", cbor), dscList)
    }

    public val validator: CertValidator by lazy { CertValidator(dscList.toTrustedCerts(), cbor) }

    public val decoder: DscListDecoder by lazy { DscListDecoder(publicKey.first()) }

    private val publicKey by lazy { application.readPemKeyAsset("covpass-sdk/dsc-list-signing-key.pem") }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    public val cbor: Cbor = defaultCbor

    internal fun init() {
        httpConfig.pinPublicKey(backendCa)
    }
}
