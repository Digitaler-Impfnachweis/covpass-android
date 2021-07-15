/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.dependencies

import de.rki.covpass.app.common.ToggleFavoriteUseCase
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CovpassDependencies] accessible.
 */
internal lateinit var covpassDeps: CovpassDependencies

/**
 * Access to various dependencies for common-app-covpass module.
 */
internal abstract class CovpassDependencies {

    private val cbor: Cbor = sdkDeps.cbor

    val certRepository: CertRepository = CertRepository(CborSharedPrefsStore("covpass_prefs", cbor))

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(certRepository) }
}
