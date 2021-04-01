package com.ibm.health.vaccination.app.main

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.vaccination.app.dependencies.certCheckerDeps
import com.ibm.health.vaccination.app.onboarding.WelcomeFragmentNav

internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (certCheckerDeps.storage.onboardingDone) {
                navigator.push(MainFragmentNav())
            } else {
                navigator.push(WelcomeFragmentNav())
            }
        }
    }
}
