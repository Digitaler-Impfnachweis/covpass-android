/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.google.android.material.tabs.TabLayout
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CovpassCheckMainBinding
import de.rki.covpass.checkapp.information.CovPassCheckInformationFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckCameraDisclosureFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.information.SettingsFragmentNav
import de.rki.covpass.commonapp.information.SettingsUpdateViewModel
import de.rki.covpass.commonapp.kronostime.TimeValidationState
import de.rki.covpass.commonapp.revocation.RevocationListUpdateViewModel
import de.rki.covpass.commonapp.storage.CheckContextRepository
import de.rki.covpass.commonapp.storage.OnboardingRepository
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted
import de.rki.covpass.sdk.utils.formatDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.ZoneId

@Parcelize
public class MainFragmentNav : FragmentNav(MainFragment::class)

/**
 * Displays the start view of the app.
 */
internal class MainFragment : BaseFragment(), DataProtectionCallback {

    private val binding by viewBinding(CovpassCheckMainBinding::inflate)
    private val revocationListUpdateViewModel by reactiveState {
        RevocationListUpdateViewModel(scope)
    }
    private val covpassCheckBackgroundViewModel by reactiveState {
        CovpassCheckBackgroundViewModel(scope)
    }
    private val settingsUpdateViewModel by reactiveState {
        SettingsUpdateViewModel(scope, true)
    }
    private val viewModel by reactiveState { MainViewModel(scope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener {
            findNavigator().push(CovPassCheckInformationFragmentNav())
        }
        binding.mainCheckCertButton.setOnClickListener {
            if (isCameraPermissionGranted(requireContext())) {
                findNavigator().push(
                    CovPassCheckQRScannerFragmentNav(
                        viewModel.isTwoGPlusOn.value,
                        viewModel.isTwoGPlusBOn.value
                    )
                )
            } else {
                findNavigator().push(
                    CovPassCheckCameraDisclosureFragmentNav(
                        viewModel.isTwoGPlusOn.value,
                        viewModel.isTwoGPlusBOn.value
                    )
                )
            }
        }
        binding.mainAvailabilityUpdateRulesLayout.setOnClickListener {
            findNavigator().push(
                SettingsFragmentNav(true)
            )
        }
        binding.mainCheckCertTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewModel.isTwoGPlusOn.value = tab?.position == 1
                    binding.mainCheckCert2gBLayout.isVisible = tab?.position == 1
                    if (tab?.position == 0) {
                        viewModel.isTwoGPlusBOn.value = false
                        binding.mainCheckCert2gBSwitch.isChecked = false
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            }
        )
        binding.mainCheckCert2gBSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isTwoGPlusBOn.value = isChecked
        }
        ViewCompat.setAccessibilityDelegate(
            binding.mainHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View?,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )
        ViewCompat.setAccessibilityDelegate(
            binding.mainCheckCertHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View?,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )
        ViewCompat.setAccessibilityDelegate(
            binding.mainAvailabilityHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View?,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )

        autoRun {
            updateAvailabilityCard(get(settingsUpdateViewModel.allUpToDate))
        }
        autoRun {
            when (val state = get(commonDeps.timeValidationRepository.state)) {
                is TimeValidationState.Failed -> {
                    binding.mainClockOutOfSync.isVisible = true
                    binding.mainClockOutOfSync.showWarning(
                        title = getString(R.string.validation_start_screen_scan_sync_message_title),
                        subtitle = getString(
                            R.string.validation_start_screen_scan_sync_message_text,
                            LocalDateTime.ofInstant(state.realTime, ZoneId.systemDefault())
                                .formatDateTime()
                        ),
                        iconRes = R.drawable.info_warning,
                    )
                }
                TimeValidationState.Success -> {
                    binding.mainClockOutOfSync.isVisible = false
                }
            }.let { }
        }
        autoRun {
            updateScannerCard(get(viewModel.isTwoGPlusOn))
        }
        autoRun {
            showActivatedRules(get(commonDeps.checkContextRepository.isDomesticRulesOn))
        }
        showNotificationIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isTwoGPlusOn.value) {
            binding.mainCheckCertTabLayout.getTabAt(1)?.select()
        } else {
            binding.mainCheckCertTabLayout.getTabAt(0)?.select()
        }
        commonDeps.timeValidationRepository.validate()
        covpassCheckBackgroundViewModel.update()
        launchWhenStarted {
            revocationListUpdateViewModel.update()
        }
    }

    override fun onDataProtectionFinish() {
        showNotificationIfNeeded()
    }

    private fun showNotificationIfNeeded() {
        when {
            commonDeps.onboardingRepository.dataPrivacyVersionAccepted.value
                != OnboardingRepository.CURRENT_DATA_PRIVACY_VERSION -> {
                findNavigator().push(DataProtectionFragmentNav())
            }
            commonDeps.checkContextRepository.checkContextNotificationVersionShown.value
                != CheckContextRepository.CURRENT_CHECK_CONTEXT_NOTIFICATION_VERSION -> {
                findNavigator().push(CheckContextNotificationFragmentNav())
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showActivatedRules(isDomesticRulesOn: Boolean) {
        if (isDomesticRulesOn) {
            binding.mainActivatedRules.text =
                String(Character.toChars(0x1F1E9) + Character.toChars(0x1F1EA)) +
                    " ${getString(R.string.startscreen_rules_tag_local)}"
        } else {
            binding.mainActivatedRules.text =
                String(Character.toChars(0x1F1EA) + Character.toChars(0x1F1FA)) +
                    " ${getString(R.string.startscreen_rules_tag_europe)}"
        }
    }

    private fun updateScannerCard(isTwoG: Boolean) {
        if (isTwoG) {
            binding.mainCheckCertHeaderTextview.setText(R.string.validation_start_screen_scan_title_2G)
            binding.mainCheckCertInfoTextview.setText(R.string.validation_start_screen_scan_message_2G)
            binding.mainCheckCertButton.setText(R.string.validation_start_screen_scan_action_button_title)
        } else {
            binding.mainCheckCertHeaderTextview.setText(R.string.validation_start_screen_scan_title)
            binding.mainCheckCertInfoTextview.setText(R.string.validation_start_screen_scan_message)
            binding.mainCheckCertButton.setText(R.string.validation_start_screen_scan_action_button_title)
        }
    }

    private fun updateAvailabilityCard(isAllUpToDate: Boolean) {
        binding.settingsSuccessBadge.isVisible = isAllUpToDate
        binding.settingsWarningBadge.isGone = isAllUpToDate
        binding.mainAvailabilityUpdateRulesDesc.setText(
            if (isAllUpToDate) {
                R.string.start_offline_link_subtitle_available
            } else {
                R.string.start_offline_subtitle_unavailable
            }
        )
    }
}
