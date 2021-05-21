/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.dependencies

import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler
import de.rki.covpass.commonapp.storage.DscRepository
import de.rki.covpass.commonapp.storage.OnboardingRepository
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore

/**
 * Global var for making the [CommonDependencies] accessible.
 */
public lateinit var commonDeps: CommonDependencies

/**
 * Access to various dependencies for common-app module.
 */
public abstract class CommonDependencies {

    /**
     * The [CommonErrorHandler].
     */
    public abstract val errorHandler: CommonErrorHandler

    public val onboardingRepository: OnboardingRepository = OnboardingRepository(
        CborSharedPrefsStore("onboarding_prefs")
    )

    public val dscRepository: DscRepository =
        DscRepository(CborSharedPrefsStore("dsc_cert_prefs"))
}
