/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.os.Bundle
import android.text.Editable
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.google.android.material.internal.CheckableImageButton
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ValidityCheckPopupContentBinding
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.isBeforeUpdateInterval
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
internal class ValidityCheckFragmentNav : FragmentNav(ValidityCheckFragment::class)

/**
 * Fragment to check the validity of all certificates for the selected country and date
 */
internal class ValidityCheckFragment :
    BaseBottomSheet(),
    ChangeCountryCallback,
    ChangeDateTimeCallback {

    private val validityCheckViewModel by reactiveState { ValidityCheckViewModel(scope) }
    private val binding by viewBinding(ValidityCheckPopupContentBinding::inflate)
    override val announcementAccessibilityRes: Int = R.string.accessibility_certificate_check_validity_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_action_button_check_validity)
        bottomSheetBinding.bottomSheetActionButton.isVisible = false
        binding.noteValidity.apply {
            text = getSpanned(R.string.certificate_check_validity_note)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }

        ValidityCertsAdapter(this).attachTo(binding.recyclerCertificates)

        autoRun {
            binding.validityCheckTravelRulesNotUpToDate.apply {
                showWarning(
                    title = getString(R.string.certificate_check_validity_travel_rules_not_up_to_title),
                    subtitle = getString(R.string.certificate_check_validity_travel_rules_not_up_to_message),
                    subtitleStyle = R.style.DefaultText_OnBackground,
                    iconRes = R.drawable.info_warning,
                    subtitleTopMarginDimenRes = R.dimen.grid_one
                )
                isVisible = get(sdkDeps.rulesUpdateRepository.lastRulesUpdate).isBeforeUpdateInterval()
            }
        }
        autoRun {
            (binding.recyclerCertificates.adapter as? ValidityCertsAdapter)?.updateList(
                get(validityCheckViewModel.validationResults)
            )
        }
        autoRun {
            val country = get(validityCheckViewModel.country)
            binding.countryValue.setText(country.nameRes)
            binding.countryValue.setOnClickListener {
                findNavigator().push(ChangeCountryFragmentNav(country.countryCode))
            }
            binding.countryValue.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = Button::class.java.name
                    info.contentDescription = getString(
                        R.string.accessibility_certificate_check_validity_selection_country,
                        getString(country.nameRes)
                    )
                }
            })
            binding.layoutCountry.findViewById<CheckableImageButton>(R.id.text_input_end_icon)
                .setAccessibilityDelegate(object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = Button::class.java.name
                        info.contentDescription =
                            getString(R.string.accessibility_certificate_check_validity_label_choose_country)
                    }
                })

            binding.layoutCountry.setEndIconOnClickListener {
                findNavigator().push(ChangeCountryFragmentNav(country.countryCode))
            }
            (binding.recyclerCertificates.adapter as? ValidityCertsAdapter)?.updateCountry(country)
        }

        autoRun {
            val time = get(validityCheckViewModel.date)
            binding.dateValue.text = Editable.Factory.getInstance().newEditable(time.formatDateTime())

            binding.dateValue.setOnClickListener {
                findNavigator().push(ChangeDateFragmentNav(time))
            }
            binding.dateValue.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = Button::class.java.name
                    info.contentDescription = getString(
                        R.string.accessibility_certificate_check_validity_selection_date,
                        time.formatDateTimeAccessibility()
                    )
                }
            })
            binding.layoutDate.findViewById<CheckableImageButton>(R.id.text_input_end_icon)
                .setAccessibilityDelegate(object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = ImageView::class.java.name
                        info.contentDescription =
                            getString(R.string.accessibility_certificate_check_validity_label_choose_date)
                    }
                })

            binding.layoutDate.setEndIconOnClickListener {
                findNavigator().push(ChangeDateFragmentNav(time))
            }
            (binding.recyclerCertificates.adapter as? ValidityCertsAdapter)?.updateDateTime(time)
        }
        autoRun { showLoading(get(loading) > 0) }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingLayout.isVisible = isLoading
        binding.recyclerCertificates.isGone = isLoading
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
    }

    override fun updateCountry(country: Country) {
        validityCheckViewModel.updateCountry(country)
    }

    override fun updateDate(dateTime: LocalDateTime) {
        validityCheckViewModel.updateDate(dateTime)
    }
}
