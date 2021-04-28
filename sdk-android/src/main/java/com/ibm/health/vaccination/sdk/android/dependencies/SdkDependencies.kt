package com.ibm.health.vaccination.sdk.android.dependencies

import android.app.Application
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.readPem
import com.ibm.health.vaccination.sdk.android.qr.QRCoder

/**
 * Global var for making the [SdkDependencies] accessible.
 */
public lateinit var sdkDeps: SdkDependencies

/**
 * Access to various dependencies for sdk-android module.
 */
public abstract class SdkDependencies {

    public abstract val application: Application

    private val ca by lazy {
        readPem(application.assets.open("default-ca.pem").bufferedReader().use { it.readText() })
    }

    private val validator by lazy { CertValidator(ca) }

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder by lazy { QRCoder(validator) }
}
