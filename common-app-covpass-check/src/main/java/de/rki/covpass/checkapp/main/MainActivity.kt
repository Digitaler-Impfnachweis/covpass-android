/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import de.rki.covpass.commonapp.BaseActivity
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.checkapp.onboarding.WelcomeFragmentNav

/**
 * The only activity in the app, hosts all fragments.
 * Initially triggers the navigation to [WelcomeFragmentNav] or [MainFragmentNav].
 */
internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (commonDeps.onboardingRepository.onboardingDone.value) {
                navigator.push(MainFragmentNav())
            } else {
                navigator.push(WelcomeFragmentNav())
            }
        }
    }
}
