/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.autoRun
import com.ensody.reactivestate.dispatchers
import com.ensody.reactivestate.get
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.navigation.android.triggerBackPress
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DisplayQrCodeBottomsheetContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.storage.GroupedCertificatesList
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize

@Parcelize
internal class DisplayQrCodeFragmentNav(val certId: String) : FragmentNav(DisplayQrCodeFragment::class)

/**
 * Fragment which displays the QR code of a vaccination on a bottom sheet.
 */
internal class DisplayQrCodeFragment : BaseBottomSheet() {

    private val args: DisplayQrCodeFragmentNav by lazy { getArgs() }

    override val buttonTextRes =
        R.string.vaccination_certificate_detail_view_qrcode_screen_action_button_title

    private val binding by viewBinding(DisplayQrCodeBottomsheetContentBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
    }

    private fun updateViews(certificateList: GroupedCertificatesList) {
        val cert = certificateList.getCombinedCertificate(args.certId) ?: return
        bottomSheetBinding.bottomSheetTitle.text = getString(
            R.string.vaccination_certificate_detail_view_qrcode_screen_title,
            cert.vaccinationCertificate.currentSeries,
            cert.vaccinationCertificate.completeSeries
        )
        launchWhenStarted {
            binding.displayQrImageview.setImageBitmap(
                generateQRCode(cert.vaccinationQrContent)
            )
        }
    }

    override fun onActionButtonClicked() {
        triggerBackPress()
    }

    private suspend fun generateQRCode(qrContent: String): Bitmap {
        return dispatchers.default {
            BarcodeEncoder().encodeBitmap(
                qrContent,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels,
                mapOf(EncodeHintType.MARGIN to 0)
            )
        }
    }
}
