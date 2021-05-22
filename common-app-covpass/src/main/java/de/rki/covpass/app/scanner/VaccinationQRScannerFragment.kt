/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.app.detail.DetailFragmentNav
import com.journeyapps.barcodescanner.BarcodeResult
import de.rki.covpass.app.R
import kotlinx.parcelize.Parcelize

@Parcelize
internal class VaccinationQRScannerFragmentNav : FragmentNav(VaccinationQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class VaccinationQRScannerFragment : QRScannerFragment(), DialogListener, VaccinationQRScannerEvents {

    private val viewModel by buildState { VaccinationQRScannerViewModel(scope, stateFlowStore) }

    override val loadingText = R.string.vaccination_add_loading_screen_message

    override fun onBarcodeResult(result: BarcodeResult) {
        viewModel.onQrContentReceived(result.text)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onScanSuccess(certificateId: String) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId))
    }
}
