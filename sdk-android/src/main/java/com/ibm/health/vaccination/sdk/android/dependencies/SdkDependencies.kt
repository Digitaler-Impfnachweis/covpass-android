package com.ibm.health.vaccination.sdk.android.dependencies

import com.ibm.health.vaccination.sdk.android.qr.QRCoder

/**
 * Global var for making the [SdkDependencies] accessible.
 */
public lateinit var sdkDeps: SdkDependencies

/**
 * Access to various dependencies for sdk-android module.
 */
public abstract class SdkDependencies {

    /**
     * The [QRCoder].
     */
    public val qrCoder: QRCoder = QRCoder()
}
