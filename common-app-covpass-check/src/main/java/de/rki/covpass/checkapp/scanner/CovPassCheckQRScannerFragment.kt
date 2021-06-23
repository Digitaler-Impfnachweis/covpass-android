/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.covpass.checkapp.validation.*
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.utils.formatOrEmpty
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
internal class CovPassCheckQRScannerFragmentNav : FragmentNav(CovPassCheckQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassCheckQRScannerFragment :
    QRScannerFragment(), DialogListener, CovPassCheckQRScannerEvents, ValidationResultListener {

    private val viewModel by reactiveState { CovPassCheckQRScannerViewModel(scope) }

    override fun onBarcodeResult(result: BarcodeResult) {
        viewModel.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onValidationSuccess(certificate: CovCertificate) {
        findNavigator().push(
            ValidationResultSuccessNav(
                certificate.fullName,
                certificate.birthDate.formatOrEmpty()
            )
        )
    }

    override fun onValidPcrTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            ValidPcrTestFragmentNav(
                certificate.fullName,
                certificate.birthDate.formatOrEmpty(),
                sampleCollection
            )
        )
    }

    override fun onValidAntigenTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            ValidAntigenTestFragmentNav(
                certificate.fullName,
                certificate.birthDate.formatOrEmpty(),
                sampleCollection
            )
        )
    }

    override fun onValidationFailure() {
        findNavigator().push(ValidationResultFailureFragmentNav())
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
    }
}
