/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.updateinfo

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.UpdateInfoBinding

public abstract class UpdateInfoFragment : BaseBottomSheet() {

    private val binding by viewBinding(UpdateInfoBinding::inflate)

    @get:StringRes
    public abstract val updateInfoPath: Int

    @get:StringRes
    public abstract val updateInfoButton: Int

    override val announcementAccessibilityRes: Int = R.string.accessibility_update_info_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetActionButton.text = getString(updateInfoButton)
        bottomSheetBinding.bottomSheetHeader.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetActionButton.isEnabled = false

        binding.loadingLayout.isVisible = true
        binding.updateInfoWebView.isGone = true
        binding.updateInfoWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                bottomSheetBinding.bottomSheetActionButton.isEnabled = true
                binding.loadingLayout.isGone = true
                binding.updateInfoWebView.isVisible = true
            }
        }
        binding.updateInfoWebView.loadUrl(getString(updateInfoPath))
    }

    override fun onActionButtonClicked() {}

    override fun onClickOutside() {}
}
