/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.scanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.app.errorhandling.ErrorHandler.Companion.TAG_ERROR_SAVING_BLOCKED
import de.rki.covpass.app.importcertificate.ImportCertificateCallback
import de.rki.covpass.app.importcertificate.ImportCertificatesFragmentNav
import de.rki.covpass.app.misuseprevention.MisusePreventionFragmentNav
import de.rki.covpass.app.ticketing.ConsentInitializationTicketingFragmentNav
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CovPassQRScannerFragmentNav : FragmentNav(CovPassQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassQRScannerFragment :
    QRScannerFragment(),
    DialogListener,
    CovPassQRScannerEvents,
    ImportCertificateCallback {

    private val viewModel by reactiveState { CovPassQRScannerViewModel(scope) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
    }

    override fun onBarcodeResult(qrCode: String) {
        viewModel.onQrContentReceived(qrCode)
    }

    override fun setupImportButton() {
        showLoading(true)
        findNavigator().push(ImportCertificatesFragmentNav())
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
        if (tag == TAG_ERROR_SAVING_BLOCKED) {
            when (action) {
                DialogAction.NEGATIVE -> {
                    findNavigator().pop()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.covpass_check_store_link)),
                        ),
                    )
                }
                DialogAction.NEUTRAL -> {
                    findNavigator().pop()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.information_faq_link)),
                        ),
                    )
                }
                else -> {
                    checkPermission()
                }
            }
        } else {
            checkPermission()
        }
    }

    override fun onScanSuccess(certificateId: GroupedCertificatesId) {
        findNavigator().popAll()
        findNavigator().push(DetailFragmentNav(certificateId, true))
    }

    override fun onTicketingQrcodeScan(ticketingDataInitialization: TicketingDataInitialization) {
        findNavigator().popAll()
        findNavigator().push(ConsentInitializationTicketingFragmentNav(ticketingDataInitialization))
    }

    override fun onLimitationWarning(qrContent: String) {
        findNavigator().popAll()
        findNavigator().push(MisusePreventionFragmentNav(qrContent))
    }

    override fun finishedImport() {
        checkPermission()
        showLoading(false)
    }
}
