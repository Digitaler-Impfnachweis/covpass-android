/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.checkerremark

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isGone
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CheckerRemarkPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.commonapp.utils.stripUnderlines
import kotlinx.parcelize.Parcelize

internal interface CheckRemarkCallback {
    fun onCheckRemarkFinish()
}

@Parcelize
internal class CheckerRemarkFragmentNav : FragmentNav(CheckerRemarkFragment::class)

/**
 * Fragment which shows a remark to CovPassCheck-App.
 */
internal class CheckerRemarkFragment : BaseBottomSheet() {

    override val buttonTextRes = R.string.certificates_start_screen_pop_up_app_reference_button
    private val binding by viewBinding(CheckerRemarkPopupContentBinding::inflate)
    override val announcementAccessibilityRes: Int = R.string.accessibility_certificate_popup_checkapp_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text =
            getString(R.string.certificates_start_screen_pop_up_app_reference_title)

        binding.checkerRemarkIllustration.isGone = resources.isLandscapeMode()
        binding.checkerRemarkFaq.apply {
            text = getSpanned(
                R.string.certificates_start_screen_pop_up_app_reference_hyperlink_linked,
                getString(R.string.covpass_check_store_link),
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.checkerRemarkContent.text = getString(R.string.certificate_start_screen_pop_up_app_reference_text)
        binding.checkerRemarkContentExplanation.text = getString(R.string.certificate_popup_checkapp_link_label)
    }

    override fun onCloseButtonClicked() {
        triggerUpdate()
    }

    override fun onActionButtonClicked() {
        triggerUpdate()
    }

    private fun triggerUpdate() {
        launchWhenStarted {
            covpassDeps.checkerRemarkRepository.checkerRemarkShown.set(
                CheckerRemarkRepository.CURRENT_CHECKER_REMARK_VERSION,
            )
            findNavigator().popUntil<CheckRemarkCallback>()?.onCheckRemarkFinish()
        }
    }
}
