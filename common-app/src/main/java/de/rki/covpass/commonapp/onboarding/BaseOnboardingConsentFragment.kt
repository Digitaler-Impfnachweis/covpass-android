/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.OnboardingConsentBinding
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.utils.isLandscapeMode

/**
 * Common base fragment for displaying a data privacy consent to the user. Both apps use basically the same fragment,
 * only the different texts and icons are defined in the app-specific fragments.
 */
public abstract class BaseOnboardingConsentFragment : BaseFragment() {

    public abstract val titleRes: Int
    public abstract val imageRes: Int
    public abstract val buttonTextRes: Int
    public abstract val dataProtectionLinkRes: Int
    public abstract val contentItemsRes: List<Int>

    public open val termsOfUseTitle: Int? = null
    public open val termsOfUseIcon: Int? = null
    public open val termsOfUseMessage: Int? = null
    public open val termsOfUseLink: Int? = null
    public open val termsOfUseLinkEvent: View.OnClickListener? = null
    public open val showTermsOfUse: Boolean = false

    private val binding by viewBinding(OnboardingConsentBinding::inflate)

    public val isScrolledToBottom: MutableValueFlow<Boolean> = MutableValueFlow(false)

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
        binding.onboardingInfoHeaderTextview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        binding.onboardingImageview.isGone = resources.isLandscapeMode()
        binding.onboardingImageview.setImageResource(imageRes)
        fillContent()
        binding.onboardingInfoDataProtectionField.apply {
            text = getString(R.string.app_information_title_datenschutz)
            setOnClickListener {
                findNavigator().push(CommonDataProtectionFragmentNav())
            }
        }
        binding.onboardingScrollView.run {
            viewTreeObserver
                .addOnScrollChangedListener {
                    isScrolledToBottom.value = (
                        getChildAt(0).bottom
                            <= (height + scrollY)
                        )
                }
        }
        if (showTermsOfUse) {
            binding.onboardingTermsOfUse.isVisible = true
            binding.onboardingTermsOfUse.showInfo(
                termsOfUseTitle?.let { it -> getString(it) } ?: "",
                termsOfUseMessage?.let { it -> getString(it) },
                R.style.DefaultText_OnBackground,
                termsOfUseLink?.let { it -> getString(it) },
                termsOfUseIcon,
                termsOfUseLinkEvent,
                R.style.Header_Info_Small,
            )
        }
    }

    private fun fillContent() {
        contentItemsRes.forEach {
            val paragraphItem = layoutInflater.inflate(
                R.layout.onboarding_consent_paragraph_item,
                binding.consentInfoItemsContainer,
                false,
            ).apply { findViewById<TextView>(R.id.content).text = getString(it) }
            binding.consentInfoItemsContainer.addView(paragraphItem)
        }
    }

    public fun scrollToBottom() {
        binding.onboardingScrollView.run {
            post { fullScroll(View.FOCUS_DOWN) }
        }
    }
}
