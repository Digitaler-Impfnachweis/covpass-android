/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dependencies

import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.commonapp.storage.OnboardingRepository
import de.rki.covpass.commonapp.truetime.TimeValidationRepository
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.getEncryptedSharedPreferences
import kotlinx.serialization.cbor.Cbor

/**
 * Global var for making the [CommonDependencies] accessible.
 */
@DependencyAccessor
public lateinit var commonDeps: CommonDependencies

@OptIn(DependencyAccessor::class)
public val LifecycleOwner.commonDeps: CommonDependencies
    get() = de.rki.covpass.commonapp.dependencies.commonDeps

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

    public val updateInfoRepository: UpdateInfoRepository = UpdateInfoRepository(
        CborSharedPrefsStore("update_info_prefs", cbor)
    )

    public val checkContextRepository: CheckContextRepository = CheckContextRepository(
        CborSharedPrefsStore("covpass_check_prefs", cbor)
    )

    public val timeValidationRepository: TimeValidationRepository = TimeValidationRepository()

    public val trueTimeSharedPrefs: SharedPreferences =
        getEncryptedSharedPreferences("true_time_shared_prefs")
}
