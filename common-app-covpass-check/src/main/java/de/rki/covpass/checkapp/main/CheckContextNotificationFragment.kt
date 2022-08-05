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
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CheckContextNotificationBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.CheckContextRepository.Companion.CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION
import kotlinx.parcelize.Parcelize

@Parcelize
public class CheckContextNotificationFragmentNav : FragmentNav(CheckContextNotificationFragment::class)

public class CheckContextNotificationFragment : BaseBottomSheet() {

    private val binding by viewBinding(CheckContextNotificationBinding::inflate)
    override val buttonTextRes: Int = R.string.check_context_onboarding_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startView()
    }

    private fun startView() {
        bottomSheetBinding.bottomSheetHeader.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false
        binding.checkContextNotificationTitle.setText(R.string.check_context_onboarding_title)
        binding.checkContextNotificationEuCheckbox.apply {
            updateValues(
                R.string.check_context_onboarding_option1_title,
                R.string.check_context_onboarding_option1_subtitle,
            )
            updateCheckbox(false)
            setOnClickListener {
                updateCheckbox(true)
                binding.checkContextNotificationLocalCheckbox.updateCheckbox(false)
            }
        }
        binding.checkContextNotificationLocalCheckbox.apply {
            updateValues(
                R.string.check_context_onboarding_option2_title,
                R.string.check_context_onboarding_option2_subtitle,
            )
            updateCheckbox(true)
            setOnClickListener {
                updateCheckbox(true)
                binding.checkContextNotificationEuCheckbox.updateCheckbox(false)
            }
        }
        binding.checkContextNotificationNote.setText(R.string.check_context_onboarding_footnote)
    }

    override fun onActionButtonClicked() {
        launchWhenStarted {
            commonDeps.checkContextRepository.isDomesticRulesOn.set(
                binding.checkContextNotificationLocalCheckbox.isChecked(),
            )
            commonDeps.checkContextRepository.checkContextNotificationVersionShown.set(
                CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION,
            )
            findNavigator().pop()
        }
    }
}
