/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ChangeCountryPopupContentBinding
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

/**
 * Interface to communicate events from [ChangeCountryFragment] back to other fragments.
 */
internal interface ChangeCountryCallback {
    fun updateCountry(country: Country)
}

@Parcelize
internal class ChangeCountryFragmentNav(val countryCode: String) : FragmentNav(ChangeCountryFragment::class)

public interface AccessibilityCallback {
    public fun updateAccessibilityFocus()
}

/**
 * Fragment to change country for the validity
 */
internal class ChangeCountryFragment : BaseBottomSheet(), AccessibilityCallback {

    private val args: ChangeCountryFragmentNav by lazy { getArgs() }
    private val countryListViewModel by reactiveState { CountryListViewModel(scope) }
    private val binding by viewBinding(ChangeCountryPopupContentBinding::inflate)
    override val announcementAccessibilityRes: Int =
        R.string.accessibility_certificate_check_validity_selection_country_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_check_validity_selection_country_title)
        bottomSheetBinding.bottomSheetActionButton.setText(
            R.string.certificate_check_validity_selection_country_action_button
        )

        val adapter = ChangeCountryAdapter(this, args.countryCode, this)
        adapter.attachTo(binding.countryList)
        autoRun {
            adapter.updateList(
                get(countryListViewModel.countries)
            )
        }
    }

    override fun onActionButtonClicked() {
        val newCountry = (binding.countryList.adapter as? ChangeCountryAdapter)?.getSelectedItem()
        newCountry?.let { findNavigator().popUntil<ChangeCountryCallback>()?.updateCountry(it) }
    }

    override fun updateAccessibilityFocus() {
        bottomSheetBinding.bottomSheetActionButton.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }
}
