/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.databinding.WelcomeBinding

/**
 * Common base fragment for displaying a welcome page to the user. Both apps use basically the same fragment,
 * only the different texts and icons are defined in the app-specific fragments.
 */
public abstract class BaseWelcomeFragment : BaseFragment() {

    private val binding by viewBinding(WelcomeBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setAccessibilityDelegate(
            binding.welcomeHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )

        ViewCompat.setAccessibilityDelegate(
            binding.welcomeEncryptionHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )
        binding.welcomeHeaderTextview.setText(getHeaderTextRes())
        binding.welcomeSubheaderTextview.setText(getSubheaderTextRes())
        binding.welcomeEncryptionHeaderTextview.setText(getEncryptionHeaderTextRes())
        binding.welcomeEncryptionTextview.setText(getEncryptionTextRes())
        binding.welcomeMainImageview.setImageResource(getMainImageRes())
        binding.welcomeStartButton.setOnClickListener { findNavigator().push(getOnboardingNav()) }
    }

    protected abstract fun getHeaderTextRes(): Int

    protected abstract fun getSubheaderTextRes(): Int

    protected abstract fun getEncryptionHeaderTextRes(): Int

    protected abstract fun getEncryptionTextRes(): Int

    protected abstract fun getMainImageRes(): Int

    protected abstract fun getOnboardingNav(): FragmentNav
}
