package com.ibm.health.vaccination.app.vaccinee.main

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.common.vaccination.app.dependencies.commonDeps
import com.ibm.health.vaccination.app.vaccinee.onboarding.WelcomeFragmentNav

/**
 * The only Activity in the app, hosts all fragments.
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
