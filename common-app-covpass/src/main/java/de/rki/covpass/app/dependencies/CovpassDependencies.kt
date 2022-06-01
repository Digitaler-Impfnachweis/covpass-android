/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.dependencies

import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.app.checkerremark.CheckerRemarkRepository
import de.rki.covpass.app.common.ToggleFavoriteUseCase
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.CertRepository
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CovpassDependencies] accessible.
 */
@DependencyAccessor
internal lateinit var covpassDeps: CovpassDependencies

@OptIn(DependencyAccessor::class)
internal val LifecycleOwner.covpassDeps: CovpassDependencies get() = de.rki.covpass.app.dependencies.covpassDeps

/**
 * Access to various dependencies for common-app-covpass module.
 */
@OptIn(DependencyAccessor::class)
internal abstract class CovpassDependencies {

    abstract val reissueErrorHandler: CommonErrorHandler

    private val cbor: Cbor get() = sdkDeps.cbor

    private val certificateListMapper get() = sdkDeps.certificateListMapper

    val certRepository: CertRepository by lazy {
        CertRepository(
            CborSharedPrefsStore("covpass_prefs", cbor),
            certificateListMapper,
        )
    }

    val checkerRemarkRepository: CheckerRemarkRepository by lazy {
        CheckerRemarkRepository(
            CborSharedPrefsStore("checker_remark_prefs", cbor),
        )
    }

    val toggleFavoriteUseCase by lazy { ToggleFavoriteUseCase(certRepository) }
}
