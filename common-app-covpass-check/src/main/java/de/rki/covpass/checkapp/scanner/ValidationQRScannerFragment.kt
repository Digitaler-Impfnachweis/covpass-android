/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.checkapp.validation.ValidationResultFailureFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultIncompleteFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultListener
import de.rki.covpass.checkapp.validation.ValidationResultSuccessFragmentNav
import de.rki.covpass.sdk.cert.models.CovCertificate
import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.covpass.checkapp.R
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ValidationQRScannerFragmentNav : FragmentNav(ValidationQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class ValidationQRScannerFragment :
    QRScannerFragment(), DialogListener, ValidationQRScannerEvents, ValidationResultListener {

    private val viewModel by buildState { ValidationQRScannerViewModel(scope) }

    override val loadingText = R.string.validation_check_loading_screen_message

    override fun onBarcodeResult(result: BarcodeResult) {
        viewModel.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onValidationSuccess(certificate: CovCertificate) {
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

    override fun onImmunizationIncomplete(certificate: CovCertificate) {
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
