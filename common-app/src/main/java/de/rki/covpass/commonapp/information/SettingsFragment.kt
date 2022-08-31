package de.rki.covpass.commonapp.information

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.CheckSettingsBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.parcelize.Parcelize

@Parcelize
public class SettingsFragmentNav(
    public val isCovPassCheck: Boolean,
) : FragmentNav(SettingsFragment::class)

public class SettingsFragment : BaseFragment(), DialogListener {

    private val binding by viewBinding(CheckSettingsBinding::inflate)
    private val args by lazy { getArgs<SettingsFragmentNav>() }
    private val settingsUpdateViewModel by reactiveState {
        SettingsUpdateViewModel(
            scope,
            args.isCovPassCheck,
        )
    }

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_app_information_title_local_rules

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()

        if (args.isCovPassCheck) {
            initCovPassCheckContent()
        } else {
            initCovPassContent()
        }
        initCommonContent()
    }

    private fun initCovPassContent() {
        binding.settingsRulesPickerLayout.isGone = true
        binding.offlineRevocationLayout.isGone = true
    }

    private fun initCovPassCheckContent() {
        val isOfflineRevocationOn =
            sdkDeps.revocationLocalListRepository.revocationListUpdateIsOn.value
        binding.offlineRevocationToggle.apply {
            updateTitle(R.string.app_information_offline_revocation_title)
            updateToggle(isOfflineRevocationOn)
            setOnClickListener {
                if (!binding.offlineRevocationToggle.isChecked()) {
                    updateToggle(!binding.offlineRevocationToggle.isChecked())
                    updateOfflineRevocationState()
                } else {
                    val dialogModel = DialogModel(
                        titleRes = R.string.app_information_offline_revocation_hint_title,
                        messageString = getString(R.string.app_information_offline_revocation_hint_copy),
                        positiveButtonTextRes = R.string.app_information_offline_revocation_hint_button_active,
                        negativeButtonTextRes = R.string.app_information_offline_revocation_hint_button_inactive,
                        tag = DELETE_REVOCATION_LIST,
                    )
                    showDialog(dialogModel, childFragmentManager)
                }
            }
        }

        val isDomesticRulesOn = commonDeps.checkContextRepository.isDomesticRulesOn.value
        binding.checkContextSettingsEuCheckbox.apply {
            updateValues(
                R.string.check_context_onboarding_option1_title,
                R.string.check_context_onboarding_option1_subtitle,
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
                R.string.check_context_onboarding_option2_subtitle,
            )
            updateCheckbox(isDomesticRulesOn)
            setOnClickListener {
                updateCheckbox(true)
                binding.checkContextSettingsEuCheckbox.updateCheckbox(false)
                updateRulesState()
            }
        }
    }

    private fun initCommonContent() {
        val adapter = SettingsAdapter(this)
        adapter.attachTo(binding.settingsRecyclerview)
        autoRun {
            adapter.updateList(
                get(settingsUpdateViewModel.settingItems),
            )
        }
        autoRun { showLoading(get(loading) > 0) }
        autoRun { showBadge(get(settingsUpdateViewModel.allUpToDate)) }

        binding.updateButton.setOnClickListener {
            settingsUpdateViewModel.update()
        }
        binding.cancelButton.setOnClickListener {
            settingsUpdateViewModel.cancel()
        }
    }

    private fun showBadge(allUpToDate: Boolean) {
        binding.settingsSuccessBadge.isVisible = allUpToDate
        binding.settingsWarningBadge.isGone = allUpToDate
    }

    private fun showLoading(isLoading: Boolean) {
        binding.settingsLoadingLayout.isVisible = isLoading
        binding.updateButton.isGone = isLoading
    }

    private fun updateRulesState() {
        launchWhenStarted {
            commonDeps.checkContextRepository.isDomesticRulesOn.set(
                binding.checkContextSettingsLocalCheckbox.isChecked(),
            )
        }
    }

    private fun updateOfflineRevocationState() {
        launchWhenStarted {
            sdkDeps.revocationLocalListRepository.revocationListUpdateIsOn.set(
                binding.offlineRevocationToggle.isChecked(),
            )
            if (binding.offlineRevocationToggle.isChecked()) {
                settingsUpdateViewModel.update()
            } else {
                settingsUpdateViewModel.deleteRevocationLocalList()
            }
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.settingsToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.settingsToolbar.setTitle(R.string.app_information_title_update)
        }
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == DELETE_REVOCATION_LIST && action == DialogAction.POSITIVE) {
            binding.offlineRevocationToggle.updateToggle(false)
            updateOfflineRevocationState()
        }
    }

    private companion object {
        const val DELETE_REVOCATION_LIST = "delete_revocation_list"
    }
}
