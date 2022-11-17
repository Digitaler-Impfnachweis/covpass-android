/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ChooseVaccinationProtectionModeBinding
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.checkapp.storage.VaccinationProtectionMode
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

internal interface ChooseVaccinationProtectionModeCallback {
    fun onModeChooseFinish()
    fun onModeChooseCancel()
}

@Parcelize
internal class ChooseVaccinationProtectionModeFragmentNav : FragmentNav(ChooseVaccinationProtectionModeFragment::class)

public class ChooseVaccinationProtectionModeFragment : BaseBottomSheet() {

    private val binding by viewBinding(ChooseVaccinationProtectionModeBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_rules_context_initial_setup_open
    override val closingAnnouncementAccessibilityRes: Int = R.string.accessibility_rules_context_initial_setup_close

    override val buttonTextRes: Int = R.string.infschg_info_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.rules_context_initial_setup_title)
        bottomSheetBinding.bottomSheetClose.isVisible = false
        binding.chooseVaccinationModeSubtitle.setText(R.string.rules_context_initial_setup_subtitle)
        binding.chooseVaccinationModeGermanyCheckbox.updateValues(
            R.string.check_context_onboarding_option2_title,
            R.string.check_context_onboarding_option2_subtitle,
        )
        binding.chooseVaccinationModeGermanyCheckbox.updateCheckbox(true)
        binding.chooseVaccinationModeGermanyCheckbox.setOnClickListener {
            binding.chooseVaccinationModeGermanyCheckbox.updateCheckbox(
                !binding.chooseVaccinationModeGermanyCheckbox.isChecked(),
            )
            binding.chooseVaccinationModeEntryCheckbox.updateCheckbox(
                !binding.chooseVaccinationModeGermanyCheckbox.isChecked(),
            )
        }
        binding.chooseVaccinationModeEntryCheckbox.updateValues(
            R.string.check_context_onboarding_option1_title,
            R.string.check_context_onboarding_option1_subtitle,
        )
        binding.chooseVaccinationModeEntryCheckbox.updateCheckbox(false)
        binding.chooseVaccinationModeEntryCheckbox.setOnClickListener {
            binding.chooseVaccinationModeEntryCheckbox.updateCheckbox(
                !binding.chooseVaccinationModeEntryCheckbox.isChecked(),
            )
            binding.chooseVaccinationModeGermanyCheckbox.updateCheckbox(
                !binding.chooseVaccinationModeEntryCheckbox.isChecked(),
            )
        }
        binding.chooseVaccinationModeInfoText.setText(R.string.rules_context_initial_setup_hint)
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            if (binding.chooseVaccinationModeGermanyCheckbox.isChecked()) {
                covpassCheckDeps.checkAppRepository.vaccinationProtectionMode.set(
                    VaccinationProtectionMode.ModeIfsg,
                )
            } else {
                covpassCheckDeps.checkAppRepository.vaccinationProtectionMode.set(
                    VaccinationProtectionMode.ModeEntryRules,
                )
            }
            covpassCheckDeps.checkAppRepository.startImmunizationStatus.set(false)
            findNavigator().popUntil<ChooseVaccinationProtectionModeCallback>()?.onModeChooseFinish()
        }
    }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<ChooseVaccinationProtectionModeCallback>()?.onModeChooseCancel()
        return Abort
    }
}
