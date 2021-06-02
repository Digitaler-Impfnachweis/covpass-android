/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.add

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.AddCovCertPopupContentBinding
import de.rki.covpass.app.scanner.CovPassQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.utils.stripUnderlines
import kotlinx.parcelize.Parcelize

@Parcelize
internal class AddCovCertificateFragmentNav : FragmentNav(AddCovCertificateFragment::class)

/**
 * Fragment which shows the instructions for QR code scan
 */
internal class AddCovCertificateFragment : BaseBottomSheet() {

    override val buttonTextRes = R.string.vaccination_add_popup_scan_button_title
    private val binding by viewBinding(AddCovCertPopupContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.vaccination_add_popup_title)
        binding.addCovCertFaq.apply {
            text = getSpanned(
                R.string.vaccination_add_popup_action_title_linked,
                getString(R.string.cert_add_popup_link)
            )
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().pop()
        findNavigator().push(CovPassQRScannerFragmentNav())
    }
}
