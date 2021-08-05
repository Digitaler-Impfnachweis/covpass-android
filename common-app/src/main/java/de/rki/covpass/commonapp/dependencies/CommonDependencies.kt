/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dependencies

import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.commonapp.storage.OnboardingRepository
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CommonDependencies] accessible.
 */
@DependencyAccessor
public lateinit var commonDeps: CommonDependencies

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.commonDeps: CommonDependencies get() = de.rki.covpass.commonapp.dependencies.commonDeps

/**
 * Access to various dependencies for common-app module.
 */
@OptIn(DependencyAccessor::class)
public abstract class CommonDependencies {

    /**
     * The [CommonErrorHandler].
     */
    public abstract val errorHandler: CommonErrorHandler

    private val cbor: Cbor = sdkDeps.cbor

    public val onboardingRepository: OnboardingRepository = OnboardingRepository(
        CborSharedPrefsStore("onboarding_prefs", cbor)
    )
}
