/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.onboarding

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.OnboardingConsentBinding

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

    private val binding by viewBinding(OnboardingConsentBinding::inflate)

    public val isScrolledToBottom: MutableValueFlow<Boolean> = MutableValueFlow(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingInfoHeaderTextview.setText(titleRes)
        binding.onboardingImageview.setImageResource(imageRes)
        fillContent()
        binding.onboardingInfoDataProtectionField.apply {
            text = getString(R.string.app_information_title_datenschutz)
            setOnClickListener {
                findNavigator().push(DataProtectionFragmentNav())
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
    }

    private fun fillContent() {
        contentItemsRes.forEach {
            val paragraphItem = layoutInflater.inflate(
                R.layout.onboarding_consent_paragraph_item,
                binding.consentInfoItemsContainer,
                false
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
