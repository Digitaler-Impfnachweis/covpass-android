/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.onboarding

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.TermsOfUseUsBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
public class OnboadingTermsOfUseUSFragmentNav : FragmentNav(OnboadingTermsOfUseUSFragment::class)

public class OnboadingTermsOfUseUSFragment : BaseBottomSheet() {

    private val binding by viewBinding(TermsOfUseUsBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingLayout.isVisible = true
        binding.onboardingTermsOfUseWebview.isGone = true

        bottomSheetBinding.bottomSheetActionButton.isGone = true
        bottomSheetBinding.bottomSheetTitle.text =
            getString(R.string.vaccination_fourth_onboarding_page_message_for_us_citizens_title)

        binding.onboardingTermsOfUseWebview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                bottomSheetBinding.bottomSheetActionButton.isEnabled = true
                binding.loadingLayout.isGone = true
                binding.onboardingTermsOfUseWebview.isVisible = true
            }
        }
        binding.onboardingTermsOfUseWebview.loadUrl(getString(R.string.terms_of_use_us))
    }

    override fun onActionButtonClicked() {}
}
