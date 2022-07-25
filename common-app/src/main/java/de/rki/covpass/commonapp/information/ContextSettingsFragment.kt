package de.rki.covpass.commonapp.information

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.CheckContextSettingsBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.revocation.RevocationListUpdateViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
public class ContextSettingsFragmentNav : FragmentNav(ContextSettingsFragment::class)

/**
 * Displays the Domestic or EU rules toggle
 */
public class ContextSettingsFragment : BaseFragment() {

    private val binding by viewBinding(CheckContextSettingsBinding::inflate)

    private val revocationListUpdateViewModel by reactiveState {
        RevocationListUpdateViewModel(scope)
    }

    override val announcementAccessibilityRes: Int = R.string.accessibility_app_information_title_local_rules

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        setupOfflineMode()
        val isDomesticRulesOn = commonDeps.checkContextRepository.isDomesticRulesOn.value
        binding.checkContextSettingsNote.setText(R.string.check_context_onboarding_footnote)
        binding.checkContextSettingsEuCheckbox.apply {
            updateValues(
                R.string.check_context_onboarding_option1_title,
                R.string.check_context_onboarding_option1_subtitle
            )
            updateCheckbox(!isDomesticRulesOn)
            setOnClickListener {
                updateCheckbox(true)
                binding.checkContextSettingsLocalCheckbox.updateCheckbox(false)
                updateRulesState()
            }
        }
        binding.checkContextSettingsLocalCheckbox.apply {
            updateValues(
                R.string.check_context_onboarding_option2_title,
                R.string.check_context_onboarding_option2_subtitle
            )
            updateCheckbox(isDomesticRulesOn)
            setOnClickListener {
                updateCheckbox(true)
                binding.checkContextSettingsEuCheckbox.updateCheckbox(false)
                updateRulesState()
            }
        }
    }

    private fun updateRulesState() {
        launchWhenStarted {
            commonDeps.checkContextRepository.isDomesticRulesOn.set(
                binding.checkContextSettingsLocalCheckbox.isChecked()
            )
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.informationToolbar.setTitle(R.string.app_information_title_local_rules)
        }
    }

    private fun setupOfflineMode() {
        binding.checkContextOfflineModeTitle.setText(R.string.app_information_offline_revocation_title)
        binding.checkContextOfflineModeNote.setText(R.string.app_information_offline_revocation_copy)
        binding.checkContextOfflineModeSwitchTitle.setText(R.string.app_information_offline_revocation_title)
        binding.checkContextOfflineModeSwitch.isChecked = revocationListUpdateViewModel.offlineModeState
        binding.checkContextOfflineModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            revocationListUpdateViewModel.offlineModeState = isChecked
        }
    }
}
