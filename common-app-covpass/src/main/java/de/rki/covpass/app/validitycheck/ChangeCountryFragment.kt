/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ChangeCountryPopupContentBinding
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryRepository
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

/**
 * Fragment to change country for the validity
 */
internal class ChangeCountryFragment : BaseBottomSheet() {

    private val args: ChangeCountryFragmentNav by lazy { getArgs() }
    private val binding by viewBinding(ChangeCountryPopupContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_check_validity_selection_country_title)
        bottomSheetBinding.bottomSheetActionButton.setText(
            R.string.certificate_check_validity_selection_country_action_button
        )

        ChangeCountryAdapter(this, args.countryCode).attachTo(binding.countryList)
        (binding.countryList.adapter as? ChangeCountryAdapter)?.updateList(CountryRepository.getSortedCountryList())
    }

    override fun onActionButtonClicked() {
        val newCountry = (binding.countryList.adapter as? ChangeCountryAdapter)?.getSelectedItem()
        newCountry?.let { findNavigator().popUntil<ChangeCountryCallback>()?.updateCountry(it) }
    }
}
