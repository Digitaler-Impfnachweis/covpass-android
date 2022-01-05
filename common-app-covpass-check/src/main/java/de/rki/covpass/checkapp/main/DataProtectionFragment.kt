/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.databinding.DataProtectionBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.OnboardingRepository
import kotlinx.parcelize.Parcelize

@Parcelize
internal class DataProtectionFragmentNav : FragmentNav(DataProtectionFragment::class)

/**
 * Fragment which represents data protection information
 */
internal class DataProtectionFragment : BaseBottomSheet() {

    override val heightLayoutParams by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    private val binding by viewBinding(DataProtectionBinding::inflate)
    override val announcementAccessibilityRes = R.string.accessibility_app_information_datenschutz_announce
    override val buttonTextRes: Int = R.string.confirmation_fourth_onboarding_page_button_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.app_information_title_datenschutz)
        binding.dataProtectionWebView.loadUrl(getString(R.string.data_protection_path))
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.onboardingRepository.dataPrivacyVersionAccepted
                .set(OnboardingRepository.CURRENT_DATA_PRIVACY_VERSION)
            findNavigator().popAll()
        }
    }

    override fun onClickOutside() {
        onActionButtonClicked()
    }

    override fun onCloseButtonClicked() {
        onActionButtonClicked()
    }
}
