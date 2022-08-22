/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.dependencies

import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.checkapp.storage.CheckAppRepository
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CovpassCheckDependencies] accessible.
 */
@DependencyAccessor
internal lateinit var covpassCheckDeps: CovpassCheckDependencies

@OptIn(DependencyAccessor::class)
internal val LifecycleOwner.covpassCheckDeps: CovpassCheckDependencies
    get() = de.rki.covpass.checkapp.dependencies.covpassCheckDeps

/**
 * Access to various dependencies for common-app-covpass module.
 */
@OptIn(DependencyAccessor::class)
internal class CovpassCheckDependencies {

    private val cbor: Cbor get() = sdkDeps.cbor

    val checkAppRepository: CheckAppRepository by lazy {
        CheckAppRepository(
            CborSharedPrefsStore("check_app_prefs", cbor),
        )
    }
}
