/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.os.Bundle
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isGone
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.BoosterNotificationPopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

internal interface BoosterNotificationCallback {
    fun onBoosterNotificationFinish()
}

@Parcelize
internal class BoosterNotificationFragmentNav : FragmentNav(BoosterNotificationFragment::class)

internal class BoosterNotificationFragment : BaseBottomSheet() {

    private val binding by viewBinding(BoosterNotificationPopupContentBinding::inflate)
    private val viewModel by reactiveState { BoosterNotificationViewModel(scope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetHeader.isGone = true
        bottomSheetBinding.bottomSheetClose.isGone = true

        ViewCompat.setAccessibilityDelegate(
            binding.notificationBoosterTitle,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )

        binding.notificationBoosterTitle.text = getString(R.string.dialog_booster_vaccination_notification_title)
        binding.notificationBoosterText.text = getString(R.string.dialog_booster_vaccination_notification_message)
        binding.notificationBoosterIconNew.text =
            getString(R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new)
        bottomSheetBinding.bottomSheetActionButton.text =
            getString(R.string.dialog_booster_vaccination_notification_button)
    }

    override fun onActionButtonClicked() {
        viewModel.updateHasSeenBoosterNotification()
        findNavigator().popUntil<BoosterNotificationCallback>()?.onBoosterNotificationFinish()
    }

    override fun onClickOutside() {
        viewModel.updateHasSeenBoosterNotification()
        findNavigator().popUntil<BoosterNotificationCallback>()?.onBoosterNotificationFinish()
    }
}
