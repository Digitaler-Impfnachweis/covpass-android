package com.ibm.health.vaccination.app.certchecker.scanner

import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.scanner.QRScannerFragment
import com.ibm.health.vaccination.app.certchecker.R
import com.ibm.health.vaccination.app.certchecker.validation.ValidationResultFailureFragmentNav
import com.ibm.health.vaccination.app.certchecker.validation.ValidationResultIncompleteFragmentNav
import com.ibm.health.vaccination.app.certchecker.validation.ValidationResultListener
import com.ibm.health.vaccination.app.certchecker.validation.ValidationResultSuccessFragmentNav
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.parcelize.Parcelize

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
@Parcelize
internal class ValidationQRScannerFragmentNav : FragmentNav(ValidationQRScannerFragment::class)

internal class ValidationQRScannerFragment :
    QRScannerFragment(), DialogListener, ValidationQRScannerEvents, ValidationResultListener {

    private val state by buildState { ValidationQRScannerState(scope) }

    override val loadingText = R.string.validation_check_loading_screen_message

    override fun onBarcodeResult(result: BarcodeResult) {
        state.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onValidationSuccess(certificate: VaccinationCertificate) {
        findNavigator().push(
            ValidationResultSuccessFragmentNav(
                certificate.fullName,
                certificate.birthDate
            )
        )
    }

    override fun onValidationFailure() {
        findNavigator().push(ValidationResultFailureFragmentNav())
    }

    override fun onImmunizationIncomplete(certificate: VaccinationCertificate) {
        findNavigator().push(
            ValidationResultIncompleteFragmentNav(
                certificate.fullName,
                certificate.birthDate
            )
        )
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
    }
}
