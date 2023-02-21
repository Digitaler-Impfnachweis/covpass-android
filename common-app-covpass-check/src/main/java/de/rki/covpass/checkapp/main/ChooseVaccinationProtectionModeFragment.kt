/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ChooseVaccinationProtectionModeBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ChooseVaccinationProtectionModeFragmentNav :
    FragmentNav(ChooseVaccinationProtectionModeFragment::class)

public class ChooseVaccinationProtectionModeFragment : BaseBottomSheet() {

    private val binding by viewBinding(ChooseVaccinationProtectionModeBinding::inflate)

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_rules_context_initial_setup_open
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_rules_context_initial_setup_close

    override val buttonTextRes: Int = R.string.infschg_info_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.rules_context_initial_setup_title)
        bottomSheetBinding.bottomSheetClose.isVisible = false
        binding.chooseVaccinationModeSubtitle.setText(R.string.rules_context_initial_setup_subtitle)
        binding.chooseVaccinationModeGermanyCheckbox.updateValues(
            R.string.settings_rules_context_germany_title,
            R.string.settings_rules_context_germany_subtitle,
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
            R.string.settings_rules_context_entry_title,
            R.string.settings_rules_context_entry_subtitle,
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
                commonDeps.checkContextRepository.vaccinationProtectionMode.set(
                    CheckContextRepository.VaccinationProtectionMode.ModeIfsg,
                )
            } else {
                commonDeps.checkContextRepository.vaccinationProtectionMode.set(
                    CheckContextRepository.VaccinationProtectionMode.ModeEntryRules,
                )
            }
        }
    }
}
