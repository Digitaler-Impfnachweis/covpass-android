/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CovPassQRScannerFragmentNav : FragmentNav(CovPassQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassQRScannerFragment : QRScannerFragment(), DialogListener, CovPassQRScannerEvents {

    private val viewModel by reactiveState { CovPassQRScannerViewModel(scope, stateFlowStore) }

    override fun onBarcodeResult(qrCode: String) {
        viewModel.onQrContentReceived(qrCode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onScanSuccess(certificateId: GroupedCertificatesId) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId, true))
    }
}
