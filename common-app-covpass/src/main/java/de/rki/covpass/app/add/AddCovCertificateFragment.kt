/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.add

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.AddCovCertPopupContentBinding
import de.rki.covpass.app.scanner.CovPassCameraDisclosureFragmentNav
import de.rki.covpass.app.scanner.CovPassQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted
import de.rki.covpass.commonapp.utils.isLandscapeMode
import kotlinx.parcelize.Parcelize

@Parcelize
internal class AddCovCertificateFragmentNav : FragmentNav(AddCovCertificateFragment::class)

/**
 * Fragment which shows the instructions for QR code scan
 */
internal class AddCovCertificateFragment : BaseBottomSheet() {

    override val buttonTextRes = R.string.certificate_add_popup_scan_button_title
    private val binding by viewBinding(AddCovCertPopupContentBinding::inflate)
    override val announcementAccessibilityRes: Int = R.string.accessibility_certificate_add_popup_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_certificate_add_popup_closing_announce

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.certificate_add_popup_title)
        binding.addCovCertIllustration.isGone = resources.isLandscapeMode()
        binding.addCovCertFaq.apply {
            setText(R.string.certificate_add_popup_action_title)
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cert_add_popup_link)))
                startActivity(browserIntent)
            }
        }
        binding.warningElement.apply {
            showWarning(
                title = getString(R.string.certificate_add_popup_note_title),
                descriptionNoLink = getString(R.string.certificate_add_popup_note_message),
                descriptionTopMarginDimenRes = R.dimen.grid_one,
            )
            binding.warningElement.isGone = requireContext().isDeviceSecure()
        }
    }

    private fun Context.isDeviceSecure(): Boolean {
        return (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure
    }

    override fun onActionButtonClicked() {
        if (isCameraPermissionGranted(requireContext())) {
            findNavigator().pop()
            findNavigator().push(CovPassQRScannerFragmentNav())
        } else {
            findNavigator().push(CovPassCameraDisclosureFragmentNav())
        }
    }
}
