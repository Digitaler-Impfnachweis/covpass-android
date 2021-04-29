package com.ibm.health.vaccination.app.vaccinee.scanner

import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.triggerBackPress
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.dialog.showDialog
import com.ibm.health.common.vaccination.app.scanner.QRScannerFragment
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.detail.DetailFragmentNav
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.parcelize.Parcelize

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
@Parcelize
class VaccinationQRScannerFragmentNav : FragmentNav(VaccinationQRScannerFragment::class)

class VaccinationQRScannerFragment : QRScannerFragment(), DialogListener, VaccinationQRScannerEvents {

    private val state by buildState { VaccinationQRScannerState(scope) }

    override val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            state.onQrContentReceived(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == DUPLICATE_CERTIFICATE_DIALOG_TAG) {
            if (action == DialogAction.NEGATIVE) {
                triggerBackPress()
            } else {
                decoratedBarcodeView.resume()
            }
        }
    }

    override fun onScanSuccess(certificateId: String) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId))
    }

    override fun onCertificateDuplicated() {
        decoratedBarcodeView.pause()
        val dialogModel = DialogModel(
            titleRes = R.string.scan_duplicate_warning_dialog_title,
            messageRes = R.string.scan_duplicate_warning_dialog_message,
            positiveButtonTextRes = R.string.scan_duplicate_warning_dialog_positive,
            negativeButtonTextRes = R.string.scan_duplicate_warning_dialog_negative,
            isCancelable = false,
            tag = DUPLICATE_CERTIFICATE_DIALOG_TAG
        )
        showDialog(dialogModel, childFragmentManager)
    }

    private companion object {
        private const val DUPLICATE_CERTIFICATE_DIALOG_TAG = "duplicate_certificate_dialog"
    }
}
