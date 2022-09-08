/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.newregulations

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.NewRegulationOnboardingPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

internal interface NewRegulationOnboardingCallback {
    fun onNewRegulationOnboardingFinish()
}

@Parcelize
internal class NewRegulationOnboardingFragmentNav : FragmentNav(NewRegulationOnboardingFragment::class)

/**
 * Fragment which shows the new regulation onboarding to CovPassCheck-App.
 */
internal class NewRegulationOnboardingFragment : BaseBottomSheet() {

    override val buttonTextRes = R.string.certificates_start_screen_pop_up_app_reference_button
    private val binding by viewBinding(NewRegulationOnboardingPopupContentBinding::inflate)
    override val announcementAccessibilityRes: Int = R.string.infschg_info_accessibility_announce_open

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.infschg_info_title)

        binding.newRegulationOnboardingNoteOne.setText(R.string.infschg_info_copy_1)
        binding.newRegulationOnboardingNoteTwo.setText(R.string.infschg_info_copy_2)
        binding.newRegulationOnboardingNoteThree.setText(R.string.infschg_info_copy_3)
    }

    override fun onCloseButtonClicked() {
        triggerUpdate()
    }

    override fun onActionButtonClicked() {
        triggerUpdate()
    }

    private fun triggerUpdate() {
        launchWhenStarted {
            covpassDeps.newRegulationRepository.newRegulationOnboardingShown.set(
                NewRegulationRepository.CURRENT_NEW_REGULATION_ONBOARDING_VERSION,
            )
            findNavigator().popUntil<NewRegulationOnboardingCallback>()?.onNewRegulationOnboardingFinish()
        }
    }
}
