package com.ibm.health.vaccination.app.vaccinee.add

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseBottomSheet
import com.ibm.health.common.vaccination.app.extensions.stripUnderlines
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.databinding.AddVaccinationCertPopupContentBinding
import com.ibm.health.vaccination.app.vaccinee.scanner.VaccinationQRScannerFragmentNav
import kotlinx.parcelize.Parcelize

@Parcelize
class AddVaccinationCertificateFragmentNav : FragmentNav(AddVaccinationCertificateFragment::class)

class AddVaccinationCertificateFragment : BaseBottomSheet() {

    override val buttonTextRes by lazy { getString(R.string.onboarding_welcome_start_button_text) }
    private val binding by viewBinding(AddVaccinationCertPopupContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.add_vaccination_cert_title)
        bottomSheetBinding.bottomSheetActionButton.setText(R.string.add_vaccination_cert_action_button_text)
        binding.addVaccinationCertFaq.apply {
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(VaccinationQRScannerFragmentNav())
    }
}
