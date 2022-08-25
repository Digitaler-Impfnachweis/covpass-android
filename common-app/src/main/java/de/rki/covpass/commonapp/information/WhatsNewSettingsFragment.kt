package de.rki.covpass.commonapp.information

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.appVersion
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.WhatsNewSettingsBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import kotlinx.parcelize.Parcelize

@Parcelize
public class WhatsNewSettingsFragmentNav : FragmentNav(WhatsNewSettingsFragment::class)

/**
 * Displays the What is new settings
 */
public class WhatsNewSettingsFragment : BaseFragment() {

    private val binding by viewBinding(WhatsNewSettingsBinding::inflate)

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_app_information_update_notifications_announce_open

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        val isPopupNotificationOn = commonDeps.updateInfoRepository.updateInfoNotificationActive.value
        binding.whatsNewSettingsNote.setText(R.string.app_information_copy_update_notifications)
        binding.whatsNewSettingsToggle.apply {
            updateTitle(R.string.app_information_update_notifications_toggle)
            updateToggle(isPopupNotificationOn)
            setOnClickListener {
                updateToggle(!binding.whatsNewSettingsToggle.isChecked())
                updatePopupNotificationSettings()
            }
        }
        binding.whatsNewTitle.text = getString(
            R.string.app_information_update_notifications_new_version_copy,
            appVersion,
        )
        binding.loadingLayout.isVisible = true
        binding.updateInfoWebView.isGone = true
        binding.updateInfoWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding.loadingLayout.isGone = true
                binding.updateInfoWebView.isVisible = true
            }
        }
        binding.updateInfoWebView.loadUrl(getString(R.string.update_info_path))
    }

    private fun updatePopupNotificationSettings() {
        launchWhenStarted {
            commonDeps.updateInfoRepository.updateInfoNotificationActive.set(
                binding.whatsNewSettingsToggle.isChecked(),
            )
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.whatsNewSettingsToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_contact_label_back)
            }
            binding.whatsNewSettingsToolbar.setTitle(R.string.app_information_title_update_notifications)
        }
    }
}
