package com.ibm.health.vaccination.app.main

import android.content.Intent
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.vaccination.app.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.onboarding.WelcomeFragmentNav

internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (vaccineeDeps.storage.onboardingDone) {
                navigator.push(MainFragmentNav())
            } else {
                navigator.push(WelcomeFragmentNav())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward ZXing's results
        if (IntentIntegrator.parseActivityResult(requestCode, resultCode, data) != null) {
            supportFragmentManager.fragments.firstOrNull()?.onActivityResult(requestCode, resultCode, data)
        }
    }
}
