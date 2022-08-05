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
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.databinding.OnboardingInfoBinding

/**
 * Common base fragment for displaying an onboarding info page to the user. Both apps use multiple variations of
 * basically the same fragment, only the different texts and icons are defined in the sub fragments.
 */
public abstract class BaseOnboardingInfoFragment : BaseFragment() {

    public abstract val titleRes: Int
    public abstract val textRes: Int
    public abstract val imageRes: Int
    public abstract val buttonTextRes: Int

    private val binding by viewBinding(OnboardingInfoBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setAccessibilityDelegate(
            binding.onboardingInfoHeaderTextview,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isHeading = true
                }
            },
        )
        binding.onboardingInfoHeaderTextview.setText(titleRes)
        binding.onboardingInfoTextview.setText(textRes)
        binding.onboardingImageview.setImageResource(imageRes)
    }
}
