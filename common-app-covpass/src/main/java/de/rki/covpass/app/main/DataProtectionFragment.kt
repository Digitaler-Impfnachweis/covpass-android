/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.databinding.DataProtectionBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.OnboardingRepository
import kotlinx.parcelize.Parcelize

internal interface DataProtectionCallback {
    fun onDataProtectionFinish()
}

@Parcelize
internal class DataProtectionFragmentNav(
    val isButtonVisible: Boolean = true
) : FragmentNav(DataProtectionFragment::class)

/**
 * Fragment which represents data protection information
 */
internal class DataProtectionFragment : BaseBottomSheet() {

    override val heightLayoutParams by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    private val binding by viewBinding(DataProtectionBinding::inflate)
    override val announcementAccessibilityRes = R.string.accessibility_app_information_datenschutz_announce
    override val buttonTextRes: Int = R.string.vaccination_fourth_onboarding_page_button_title
    private val args: DataProtectionFragmentNav by lazy { getArgs() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.app_information_title_datenschutz)
        binding.dataProtectionWebView.loadUrl(getString(R.string.data_protection_path))
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = args.isButtonVisible
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.onboardingRepository.dataPrivacyVersionAccepted
                .set(OnboardingRepository.CURRENT_DATA_PRIVACY_VERSION)
            findNavigator().popUntil<DataProtectionCallback>()?.onDataProtectionFinish()
        }
    }

    override fun onClickOutside() {
        onActionButtonClicked()
    }

    override fun onCloseButtonClicked() {
        onActionButtonClicked()
    }
}
