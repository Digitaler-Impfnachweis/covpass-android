/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import de.rki.covpass.app.onboarding.WelcomeFragmentNav
import de.rki.covpass.commonapp.BaseActivity
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.OnboardingRepository.Companion.FIRST_DATA_PRIVACY_VERSION
import de.rki.covpass.commonapp.updateinfo.UpdateInfoRepository.Companion.CURRENT_UPDATE_VERSION

/**
 * The only Activity in the app, hosts all fragments.
 * Initially triggers the navigation to [WelcomeFragmentNav] or [MainFragmentNav].
 */
internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchWhenStarted {
            if (commonDeps.updateInfoRepository.updateInfoVersionShown.value == 0) {
                commonDeps.updateInfoRepository.updateInfoVersionShown.set(CURRENT_UPDATE_VERSION)
            }
        }
        val uri = onSharedIntent()
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (commonDeps.onboardingRepository.dataPrivacyVersionAccepted.value != FIRST_DATA_PRIVACY_VERSION) {
                navigator.push(MainFragmentNav(uri))
            } else {
                navigator.push(WelcomeFragmentNav(uri))
            }
        }
    }

    private fun onSharedIntent(): Uri? =
        if (intent.action == Intent.ACTION_SEND) {
            intent.clipData?.getItemAt(0)?.uri
        } else {
            null
        }
}
