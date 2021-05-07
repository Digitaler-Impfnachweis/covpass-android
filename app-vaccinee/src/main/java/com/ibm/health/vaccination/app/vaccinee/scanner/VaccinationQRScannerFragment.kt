package com.ibm.health.vaccination.app.vaccinee.scanner

import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.errorhandling.CommonErrorHandler.Companion.TAG_ERROR_CONNECTION
import com.ibm.health.common.vaccination.app.scanner.QRScannerFragment
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.detail.DetailFragmentNav
import com.ibm.health.vaccination.app.vaccinee.errorhandling.ErrorHandler.Companion.TAG_ERROR_DUPLICATE_CERTIFICATE
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.parcelize.Parcelize

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
@Parcelize
class VaccinationQRScannerFragmentNav : FragmentNav(VaccinationQRScannerFragment::class)

class VaccinationQRScannerFragment : QRScannerFragment(), DialogListener, VaccinationQRScannerEvents {

    private val state by buildState { VaccinationQRScannerState(scope, stateFlowStore) }

    override val loadingText = R.string.vaccination_add_loading_screen_message

    override fun onBarcodeResult(result: BarcodeResult) {
        state.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == TAG_ERROR_DUPLICATE_CERTIFICATE && action == DialogAction.NEGATIVE) {
            findNavigator().pop()
        } else if (tag == TAG_ERROR_CONNECTION) {
            state.lastCertificateId.value?.also {
                onScanSuccess(it)
            } ?: run {
                // This should not be possible, just as a safety fallback
                findNavigator().pop()
            }
        } else {
            scanEnabled.value = true
        }
    }

    override fun onScanSuccess(certificateId: String) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId))
    }
}
