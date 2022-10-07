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
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CovpassCheckMainBinding
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.checkapp.federalstate.ChangeFederalStateFragmentNav
import de.rki.covpass.checkapp.federalstate.FederalStateResolver
import de.rki.covpass.checkapp.information.CovPassCheckInformationFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckCameraDisclosureFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.information.SettingsFragmentNav
import de.rki.covpass.commonapp.information.SettingsUpdateViewModel
import de.rki.covpass.commonapp.kronostime.TimeValidationState
import de.rki.covpass.commonapp.revocation.RevocationListUpdateViewModel
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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener {
            findNavigator().push(CovPassCheckInformationFragmentNav())
        }
        binding.mainCheckCertButton.setOnClickListener {
            if (isCameraPermissionGranted(requireContext())) {
                findNavigator().push(
                    CovPassCheckQRScannerFragmentNav(),
                )
            } else {
                findNavigator().push(CovPassCheckCameraDisclosureFragmentNav())
            }
        }
        binding.mainAvailabilityUpdateRulesLayout.setOnClickListener {
            findNavigator().push(
                SettingsFragmentNav(true),
            )
        }
        binding.mainCheckCertButton.setText(R.string.validation_start_screen_scan_action_button_title)
        binding.federalStateTitle.setText(R.string.infschg_start_screen_dropdown_title)
        binding.federalStateLayout.setOnClickListener {
            findNavigator().push(ChangeFederalStateFragmentNav(covpassCheckDeps.checkAppRepository.federalState.value))
        }

        ViewCompat.setAccessibilityDelegate(
            binding.mainHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat,
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )
        ViewCompat.setAccessibilityDelegate(
            binding.federalStateTitle,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat,
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )
        ViewCompat.setAccessibilityDelegate(
            binding.mainAvailabilityHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat,
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )

        autoRun {
            FederalStateResolver.getFederalStateByCode(
                get(covpassCheckDeps.checkAppRepository.federalState),
            )?.nameRes?.let {
                binding.federalStateName.setText(it)
            }
        }
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
                                .formatDateTime(),
                        ),
                        iconRes = R.drawable.info_warning,
                    )
                }
                TimeValidationState.Success -> {
                    binding.mainClockOutOfSync.isVisible = false
                }
            }.let { }
        }
        showNotificationIfNeeded()
    }

    override fun onResume() {
        super.onResume()
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
            !covpassCheckDeps.checkAppRepository.newRegulationNotificationShown.value -> {
                findNavigator().push(NewRegulationNotificationFragmentNav())
            }
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
            },
        )
    }
}
