/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.dependencies

import android.app.Application
import de.rki.covpass.http.httpConfig
import de.rki.covpass.http.pinPublicKey
import de.rki.covpass.sdk.android.cert.QRCoder
import de.rki.covpass.sdk.android.crypto.CertValidator
import de.rki.covpass.sdk.android.crypto.readPemAsset

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

    private val backendCa by lazy { application.readPemAsset("backend-ca.pem") }

    private val dscList by lazy { application.readPemAsset("dsc-list.pem") }

    private val validator by lazy { CertValidator(dscList) }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }

    internal fun init() {
        httpConfig.pinPublicKey(backendCa)
    }
}
