package com.ibm.health.vaccination.app.vaccinee.main

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.onboarding.WelcomeFragmentNav

internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (vaccineeDeps.storage.onboardingDone.value) {
                navigator.push(MainFragmentNav())
            } else {
                navigator.push(WelcomeFragmentNav())
            }
        }
    }
}
