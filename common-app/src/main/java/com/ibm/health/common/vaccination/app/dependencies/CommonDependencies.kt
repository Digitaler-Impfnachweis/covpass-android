/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.vaccination.app.dependencies

import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler
import com.ibm.health.common.vaccination.app.storage.DscRepository
import com.ibm.health.common.vaccination.app.storage.OnboardingRepository
import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore

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
