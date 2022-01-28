/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.information

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.appVersion
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.InformationBinding
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.onboarding.OnboardingDataProtectionFragmentNav
import de.rki.covpass.commonapp.utils.stripUnderlines
import java.util.*

/**
 * Common base fragment to display the faq, imprint etc. Both apps use the same fragment, only the different links are
 * defined inside the app-specific fragments.
 */
public abstract class InformationFragment : BaseFragment() {

    private val binding by viewBinding(InformationBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_app_information_title_informationt_announce

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        binding.informationAppVersionLabel.text = getString(R.string.app_information_version_label, appVersion)
        binding.informationAppVersionLabel.contentDescription = getString(
            R.string.app_information_version_label,
            appVersion.replace(".", getString(R.string.accessibility_app_information_version_number_delimiter))
        )
        if (Locale.getDefault().language == Locale.GERMAN.language) {
            binding.informationFieldEasyLanguage.apply {
                text = getSpanned(
                    R.string.app_information_title_company_easy_language_linked,
                    getString(getEasyLanguageLinkRes())
                )
                movementMethod = LinkMovementMethod.getInstance()
                stripUnderlines()
            }
            binding.informationFieldEasyLanguage.isVisible = true
            binding.dividerEasyLanguage.isVisible = true
        }
        binding.informationFieldFaq.apply {
            text = getSpanned(
                R.string.app_information_title_faq_linked,
                getString(getFAQLinkRes())
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.informationFieldDataSecurityPolicy.apply {
            text = getString(R.string.app_information_title_datenschutz)
            setOnClickListener {
                findNavigator().push(OnboardingDataProtectionFragmentNav())
            }
        }
        binding.informationFieldImprint.apply {
            text = getSpanned(
                R.string.app_information_title_company_details_linked,
                getString(getImprintLinkRes())
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }

        binding.informationFieldOpenSourceLicenses.apply {
            text = getString(R.string.app_information_title_open_source)
            setOnClickListener {
                findNavigator().push(OpenSourceLicenseFragmentNav())
            }
        }

        binding.informationFieldContacts.apply {
            setText(R.string.app_information_title_contact)
            setOnClickListener {
                findNavigator().push(ContactsFragmentNav())
            }
        }

        binding.informationFieldAppRulesUpdate.apply {
            setText(R.string.app_information_title_update)
            setOnClickListener {
                findNavigator().push(AppRulesUpdateFragment())
            }
        }

        if (isCovpassCheck()) {
            binding.informationFieldContextSettingsLayout.setOnClickListener {
                findNavigator().push(ContextSettingsFragment())
            }
            binding.informationFieldContextSettingsTitle.setText(
                R.string.app_information_title_local_rules
            )
            binding.informationFieldContextSettingsStatus.setText(
                if (commonDeps.checkContextRepository.isDomesticRulesOn.value) {
                    R.string.app_information_title_local_rules_status_DE
                } else {
                    R.string.app_information_title_local_rules_status_EU
                }
            )
        }
    }

    private fun setupActionBar() {
        attachToolbar(binding.informationToolbar)
        (activity as? AppCompatActivity)?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.back_arrow)
                setHomeActionContentDescription(R.string.accessibility_app_information_label_back)
            }
            binding.informationToolbar.setTitle(R.string.app_information_title)
        }
    }

    protected abstract fun getFAQLinkRes(): Int
    protected abstract fun getImprintLinkRes(): Int
    protected abstract fun getEasyLanguageLinkRes(): Int
    protected abstract fun isCovpassCheck(): Boolean
}
