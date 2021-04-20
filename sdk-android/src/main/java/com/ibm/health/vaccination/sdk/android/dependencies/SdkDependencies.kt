package com.ibm.health.vaccination.sdk.android.dependencies

import com.ibm.health.vaccination.sdk.android.qr.QRCoder
import kotlinx.serialization.ExperimentalSerializationApi

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
    @ExperimentalSerializationApi
    public val qrCoder: QRCoder = QRCoder()
}
