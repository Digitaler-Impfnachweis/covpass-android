/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.DataProtectionBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.app_information_title_datenschutz)
        bottomSheetBinding.bottomSheetActionButton.isVisible = false
        binding.dataProtectionWebView.loadUrl(getString(R.string.data_protection_path))
    }

    override fun onActionButtonClicked() {}
}
