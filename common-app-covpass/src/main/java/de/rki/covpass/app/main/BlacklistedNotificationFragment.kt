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
import de.rki.covpass.app.databinding.BlacklistNotificationPopupContentBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
internal class BlackListedNotificationFragmentNav : FragmentNav(BlackListedNotificationFragment::class)

internal class BlackListedNotificationFragment : BlacklistedNotificationEvents, BaseBottomSheet() {

    private val binding by viewBinding(BlacklistNotificationPopupContentBinding::inflate)
    private val viewModel by reactiveState { BlacklistedNotificationViewModel(scope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetHeader.isGone = true
        bottomSheetBinding.bottomSheetClose.isGone = true

        ViewCompat.setAccessibilityDelegate(
            binding.notificationBlacklistTitle,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            }
        )

        binding.notificationBlacklistTitle.text = getString(R.string.certificate_abda_incident_notification_title)
        binding.notificationBlacklistText.text = getString(R.string.certificate_abda_incident_notification_message)
        binding.notificationBlacklistIconNew.text =
            getString(R.string.certificate_abda_incident_notification_icon_new)
        bottomSheetBinding.bottomSheetActionButton.text =
            getString(R.string.certificate_abda_incident_notification_button)
    }

    override fun onActionButtonClicked() {
        viewModel.updateHasSeenBlacklistedNotification()
    }

    override fun onClickOutside() {
        viewModel.updateHasSeenBlacklistedNotification()
    }

    override fun onUpdatedBlacklistedNotification() {
        findNavigator().pop()
    }
}
