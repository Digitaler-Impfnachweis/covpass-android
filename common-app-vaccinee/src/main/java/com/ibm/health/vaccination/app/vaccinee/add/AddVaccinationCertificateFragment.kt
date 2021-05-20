/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.vaccinee.add

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseBottomSheet
import com.ibm.health.common.vaccination.app.utils.stripUnderlines
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.databinding.AddVaccinationCertPopupContentBinding
import com.ibm.health.vaccination.app.vaccinee.scanner.VaccinationQRScannerFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
internal class AddVaccinationCertificateFragmentNav : FragmentNav(AddVaccinationCertificateFragment::class)

/**
 * Fragment which shows the instructions for QR code scan
 */
internal class AddVaccinationCertificateFragment : BaseBottomSheet() {

    override val buttonTextRes by lazy { R.string.vaccination_add_popup_scan_button_title }
    private val binding by viewBinding(AddVaccinationCertPopupContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.vaccination_add_popup_title)
        binding.addVaccinationCertFaq.apply {
            text = getSpanned(
                R.string.vaccination_add_popup_action_title_linked,
                getString(R.string.vaccination_add_popup_link)
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(VaccinationQRScannerFragmentNav())
    }
}
