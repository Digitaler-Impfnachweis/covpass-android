/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ValidityCheckPopupContentBinding
import de.rki.covpass.app.main.MainFragment
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.errorhandling.isNoInternetError
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.utils.formatDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
internal class ValidityCheckFragmentNav : FragmentNav(ValidityCheckFragment::class)

/**
 * Fragment to check the validity of all certificates for the selected country and date
 */
internal class ValidityCheckFragment :
    DialogListener,
    BaseBottomSheet(),
    ChangeCountryCallback,
    ChangeDateTimeCallback {

    private val validityCheckViewModel by reactiveState { ValidityCheckViewModel(scope) }
    private val binding by viewBinding(ValidityCheckPopupContentBinding::inflate)

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
            (binding.recyclerCertificates.adapter as? ValidityCertsAdapter)?.updateList(
                get(validityCheckViewModel.validationResults)
            )
        }
        autoRun {
            val country = get(validityCheckViewModel.country)
            binding.countryValue.setText(country.nameRes)
            binding.layoutCountry.setOnClickListener {
                findNavigator().push(ChangeCountryFragmentNav(country.countryCode))
            }
            (binding.recyclerCertificates.adapter as? ValidityCertsAdapter)?.updateCountry(country)
        }
        autoRun {
            val time = get(validityCheckViewModel.date)
            binding.dateValue.text = time.formatDateTime()
            binding.layoutDate.setOnClickListener {
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

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == NO_INTERNET_CONNECTION && action == DialogAction.POSITIVE) {
            validityCheckViewModel.loadRulesAndValidateCertificates()
        } else {
            findNavigator().popUntil<MainFragment>()
        }
    }

    override fun onError(error: Throwable) {
        if (isNoInternetError(error)) {
            val dialogModel = DialogModel(
                titleRes = R.string.error_check_validity_no_internet_title,
                messageString = getString(R.string.error_check_validity_no_internet_message),
                positiveButtonTextRes = R.string.error_check_validity_no_internet_button_try_again,
                negativeButtonTextRes = R.string.error_check_validity_no_internet_button_cancel,
                tag = NO_INTERNET_CONNECTION
            )
            showDialog(dialogModel, childFragmentManager)
        } else {
            super.onError(error)
        }
    }

    companion object {
        private const val NO_INTERNET_CONNECTION = "no_internet_connection"
    }
}
