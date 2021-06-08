/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.covpass.checkapp.validation.*
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
internal class CovPassCheckQRScannerFragmentNav : FragmentNav(CovPassCheckQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassCheckQRScannerFragment :
    QRScannerFragment(), DialogListener, CovPassCheckQRScannerEvents, ValidationResultListener {

    private val viewModel by buildState { CovPassCheckQRScannerViewModel(scope) }

    override fun onBarcodeResult(result: BarcodeResult) {
        viewModel.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onFullVaccination(certificate: CovCertificate) {
        findNavigator().push(
            FullVaccinationFragmentNav(
                certificate.fullName,
                certificate.birthDate
            )
        )
    }

    override fun onPartialVaccination() {
        findNavigator().push(PartialVaccinationFragmentNav())
    }

    override fun onNegativeValidPcrTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            NegativeValidPcrTestFragmentNav(
                certificate.fullName,
                certificate.birthDate,
                sampleCollection
            )
        )
    }

    override fun onNegativeExpiredPcrTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            NegativeExpiredPcrTestFragmentNav(
                certificate.fullName,
                certificate.birthDate,
                sampleCollection
            )
        )
    }

    override fun onPositivePcrTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            PositivePcrTestFragmentNav(
                certificate.fullName,
                certificate.birthDate,
                sampleCollection
            )
        )
    }

    override fun onNegativeValidAntigenTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            NegativeValidAntigenTestFragmentNav(
                certificate.fullName,
                certificate.birthDate,
                sampleCollection
            )
        )
    }

    override fun onNegativeExpiredAntigenTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            NegativeExpiredAntigenTestFragmentNav(
                certificate.fullName,
                certificate.birthDate,
                sampleCollection
            )
        )
    }

    override fun onPositiveAntigenTest(
        sampleCollection: ZonedDateTime?
    ) {
        findNavigator().push(
            PositiveAntigenTestFragmentNav(
                sampleCollection
            )
        )
    }

    override fun onValidRecoveryCert(
        certificate: CovCertificate
    ) {
        findNavigator().push(
            ValidRecoveryCertFragmentNav(
                certificate.fullName,
                certificate.birthDate
            )
        )
    }

    override fun onExpiredRecoveryCert() {
        findNavigator().push(ExpiredRecoveryCertFragmentNav())
    }

    override fun onValidationFailure() {
        findNavigator().push(ValidationResultFailureFragmentNav())
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
    }
}
