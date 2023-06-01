/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.appVersion
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.InformationBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.onboarding.CommonDataProtectionFragmentNav
import de.rki.covpass.sdk.utils.SunsetChecker
import java.util.Locale

/**
 * Common base fragment to display the faq, imprint etc. Both apps use the same fragment, only the different links are
 * defined inside the app-specific fragments.
 */
public abstract class InformationFragment : BaseFragment() {

    private val binding by viewBinding(InformationBinding::inflate)

    override val announcementAccessibilityRes: Int =
        R.string.accessibility_app_information_title_informationt_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_app_information_title_information_closing_announce

    @SuppressLint("StringFormatInvalid", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        val isSunset = SunsetChecker.isSunset()

        binding.informationAppVersionLabel.text =
            getString(R.string.app_information_version_label, appVersion)
        binding.informationAppVersionLabel.contentDescription = getString(
            R.string.app_information_version_label,
            appVersion.replace(
                ".",
                getString(R.string.accessibility_app_information_version_number_delimiter),
            ),
        )
        if (Locale.getDefault().language == Locale.GERMAN.language) {
            binding.informationFieldEasyLanguage.apply {
                setText(R.string.app_information_title_company_easy_language)
                setOnClickListener {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(getString(getEasyLanguageLinkRes())))
                    startActivity(browserIntent)
                }
            }
            binding.informationFieldEasyLanguage.isVisible = true
            binding.dividerEasyLanguage.isVisible = true
        }
        binding.informationFieldEasyLanguage.isGone = isSunset
        binding.dividerEasyLanguage.isGone = isSunset

        binding.informationFieldFaq.apply {
            if (isSunset) {
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_external_link)
                setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
            }
            setText(R.string.app_information_title_faq)
            setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(getString(getFAQLinkRes())))
                startActivity(browserIntent)
            }
        }
        binding.informationFieldDataSecurityPolicy.apply {
            text = getString(R.string.app_information_title_datenschutz)
            setOnClickListener {
                findNavigator().push(CommonDataProtectionFragmentNav())
            }
        }
        binding.informationFieldImprint.apply {
            setText(R.string.app_information_title_company_details)
            setOnClickListener {
                findNavigator().push(ImpressumFragmentNav())
            }
        }

        binding.informationFieldOpenSourceLicenses.apply {
            text = getString(R.string.app_information_title_open_source)
            setOnClickListener {
                findNavigator().push(OpenSourceLicenseFragmentNav())
            }
        }

        binding.informationFieldAppRulesUpdate.apply {
            setText(R.string.app_information_title_update)
            setOnClickListener {
                findNavigator().push(SettingsFragmentNav(isCovpassCheck()))
            }
        }

        binding.informationFieldAccessibilityStatement.apply {
            setText(R.string.app_information_title_accessibility_statement)
            setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.information_accessibility_statement)),
                )
                startActivity(browserIntent)
            }
        }
        binding.informationFieldAccessibilityStatement.isGone = isSunset
        binding.dividerInformationFieldAccessibilityStatement.isGone = isSunset

        binding.informationFieldCovpassWhatsNewSettingsContainer.isGone = isSunset
        binding.informationFieldCovpassWhatsNewSettingsLayout.setOnClickListener {
            findNavigator().push(WhatsNewSettingsFragmentNav())
        }
        binding.informationFieldCovpassWhatsNewSettingsTitle.setText(
            R.string.app_information_title_update_notifications,
        )
        binding.informationFieldCovpassWhatsNewSettingsStatus.setText(
            if (commonDeps.updateInfoRepository.updateInfoNotificationActive.value) {
                R.string.settings_list_status_on
            } else {
                R.string.settings_list_status_off
            },
        )

        if (isCovpassCheck()) {
            binding.informationFieldCovpassCheckSettingsContainer.isGone = isSunset
            if (isSunset) {
                resetExpertMode()
            }
            binding.informationFieldExpertModeLayout.setOnClickListener {
                findNavigator().push(ExpertModeSettingsFragmentNav())
            }
            binding.informationFieldExpertModeTitle.setText(
                R.string.app_information_authorities_function_title,
            )
            binding.informationFieldExpertModeStatus.setText(
                if (commonDeps.checkContextRepository.isExpertModeOn.value) {
                    R.string.app_information_authorities_function_state_on
                } else {
                    R.string.app_information_authorities_function_state_off
                },
            )

            binding.informationFieldAcousticFeedbackContainer.isVisible = true
            binding.informationFieldAcousticFeedbackLayout.setOnClickListener {
                findNavigator().push(AcousticFeedbackFragmentNav())
            }
            binding.informationFieldAcousticFeedbackTitle.setText(
                R.string.app_information_beep_when_checking_title,
            )
            binding.informationFieldAcousticFeedbackStatus.setText(
                if (commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.value) {
                    R.string.on
                } else {
                    R.string.off
                },
            )
        } else {
            binding.informationFieldAcousticFeedbackLayout.isVisible = false
            binding.informationFieldCovpassCheckSettingsContainer.isVisible = false
        }
    }

    private fun resetExpertMode() {
        launchWhenStarted {
            commonDeps.checkContextRepository.isExpertModeOn.set(false)
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow_big)
                setHomeActionContentDescription(R.string.accessibility_app_information_label_back)
            }
            binding.informationToolbar.setTitle(R.string.app_information_title)
            binding.informationToolbar.getChildAt(1).foreground =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_highlight_selector, null)
        }
    }

    protected abstract fun getFAQLinkRes(): Int
    protected abstract fun getEasyLanguageLinkRes(): Int
    protected abstract fun isCovpassCheck(): Boolean
    protected open fun hasAcousticFeedback(): Boolean = false
}
