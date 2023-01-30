/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isGone
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.FederalStateOnboardingPopupContentBinding
import de.rki.covpass.app.information.FederalStateSettingsViewModel
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.federalstate.ChangeFederalStateCallBack
import de.rki.covpass.commonapp.federalstate.ChangeFederalStateFragmentNav
import de.rki.covpass.commonapp.utils.FederalStateResolver
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.commonapp.utils.setExternalLinkImage
import kotlinx.parcelize.Parcelize

internal interface FederalStateOnboardingCallback {
    fun onFederalStateOnboardingFinish()
}

@Parcelize
internal class FederalStateOnboardingFragmentNav : FragmentNav(FederalStateOnboardingFragment::class)

internal class FederalStateOnboardingFragment : BaseBottomSheet(), ChangeFederalStateCallBack {

    private val binding by viewBinding(FederalStateOnboardingPopupContentBinding::inflate)
    private val viewModel by reactiveState { FederalStateSettingsViewModel(scope) }
    override val buttonTextRes: Int = R.string.ok
    override val announcementAccessibilityRes: Int = R.string.accessibility_popup_choose_federal_state_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_popup_choose_federal_state_closing_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.infschg_popup_choose_federal_state_title)

        binding.federalStateOnboardingIllustration.isGone = resources.isLandscapeMode()
        binding.federalStateOnboardingNote.apply {
            text = getSpanned(R.string.infschg_popup_choose_federal_state_copy_2)
            movementMethod = LinkMovementMethod.getInstance()
            setExternalLinkImage()
        }
        binding.federalStateOnboardingValue.setOnClickListener {
            findNavigator().push(
                ChangeFederalStateFragmentNav(commonDeps.federalStateRepository.federalState.value),
            )
        }
        autoRun {
            FederalStateResolver.getFederalStateByCode(
                get(commonDeps.federalStateRepository.federalState),
            )?.let {
                binding.federalStateOnboardingValue.updateText(
                    getString(it.nameRes),
                )
            }
        }
        autoRun {
            get(viewModel.maskRuleValidFrom)?.let { validFrom ->
                if (validFrom.isNotBlank()) {
                    binding.federalStateOnboardingValue.updateDescription(
                        getString(
                            R.string.infschg_popup_choose_federal_state_copy_3,
                            validFrom,
                        ),
                    )
                } else {
                    binding.federalStateOnboardingValue.updateDescription(null)
                }
            }
        }
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.federalStateRepository.federalStateOnboardingShown.set(true)
            findNavigator().popUntil<FederalStateOnboardingCallback>()?.onFederalStateOnboardingFinish()
        }
    }

    override fun onClickOutside() {
        launchWhenStarted {
            commonDeps.federalStateRepository.federalStateOnboardingShown.set(true)
            findNavigator().popUntil<FederalStateOnboardingCallback>()?.onFederalStateOnboardingFinish()
        }
    }

    override fun onChangeDone() {
        viewModel.onFederalStateChanged()
    }
}
