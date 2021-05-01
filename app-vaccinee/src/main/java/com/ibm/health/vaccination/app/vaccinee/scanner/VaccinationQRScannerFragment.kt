package com.ibm.health.vaccination.app.vaccinee.scanner

import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.CommonErrorHandler.Companion.TAG_ERROR_GENERAL
import com.ibm.health.vaccination.app.vaccinee.errorhandling.ErrorHandler.Companion.TAG_ERROR_DUPLICATE_CERTIFICATE
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.triggerBackPress
import com.ibm.health.common.vaccination.app.CommonErrorHandler.Companion.TAG_ERROR_CONNECTION
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.scanner.QRScannerFragment
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
            decoratedBarcodeView.pause()
            state.onQrContentReceived(result.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        when (tag) {
            TAG_ERROR_DUPLICATE_CERTIFICATE ->
                if (action == DialogAction.NEGATIVE) {
                    triggerBackPress()
                } else {
                    decoratedBarcodeView.resume()
                }
            TAG_ERROR_CONNECTION ->
                arguments?.getString(ARG_KEY_CERT_ID)?.let {
                    onScanSuccess(it)
                } ?: run {
                    // This should not be possible, just as a safety fallback
                    triggerBackPress()
                }
            TAG_ERROR_GENERAL -> decoratedBarcodeView.resume()
        }
    }

    override fun onScanSuccess(certificateId: String) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId))
    }

    override fun onScanConnectionError(connectionError: Throwable, certificateId: String) {
        arguments?.putString(ARG_KEY_CERT_ID, certificateId)
        super.onError(connectionError)
    }

    private companion object {
        const val ARG_KEY_CERT_ID = "cert_id"
    }
}
