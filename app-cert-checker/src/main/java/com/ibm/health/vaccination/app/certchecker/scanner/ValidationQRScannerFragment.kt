package com.ibm.health.vaccination.app.certchecker.scanner

import androidx.lifecycle.LifecycleObserver
import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.triggerBackPress
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.dialog.showDialog
import com.ibm.health.common.vaccination.app.scanner.QRScannerFragment
import com.ibm.health.vaccination.app.certchecker.R
import com.ibm.health.vaccination.sdk.android.qr.models.ValidationCertificate
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.parcelize.Parcelize

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
@Parcelize
class ValidationQRScannerFragmentNav : FragmentNav(ValidationQRScannerFragment::class)

class ValidationQRScannerFragment : QRScannerFragment(), LifecycleObserver, DialogListener, ValidationQRScannerEvents {

    private val state by buildState { ValidationQRScannerState(scope) }

    override val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            decoratedBarcodeView.pause()
            state.onQrContentReceived(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == CERTIFICATE_PREVIEW_DIALOG_TAG) {
            triggerBackPress()
        }
    }

    override fun onValidationSuccess(certificate: ValidationCertificate) {
        // TODO implement
        val dialogModel = DialogModel(
            titleRes = null,
            messageRes = R.string.validation_certificate_preview_dialog_message,
            messageParameter = "onValidationSuccess\n\n$certificate",
            positiveButtonTextRes = R.string.validation_certificate_preview_dialog_positive,
            tag = CERTIFICATE_PREVIEW_DIALOG_TAG
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onValidationFailure() {
        // TODO implement
        val dialogModel = DialogModel(
            titleRes = null,
            messageRes = R.string.validation_certificate_preview_dialog_message,
            messageParameter = "onValidationFailure",
            positiveButtonTextRes = R.string.validation_certificate_preview_dialog_positive,
            tag = CERTIFICATE_PREVIEW_DIALOG_TAG
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onImmunizationIncomplete(certificate: ValidationCertificate) {
        // TODO implement
        val dialogModel = DialogModel(
            titleRes = null,
            messageRes = R.string.validation_certificate_preview_dialog_message,
            messageParameter = "onImmunizationIncomplete\n\n$certificate",
            positiveButtonTextRes = R.string.validation_certificate_preview_dialog_positive,
            tag = CERTIFICATE_PREVIEW_DIALOG_TAG
        )
        showDialog(dialogModel, childFragmentManager)
    }

    private companion object {
        private const val CERTIFICATE_PREVIEW_DIALOG_TAG = "certificate_preview_dialog"
    }
}
