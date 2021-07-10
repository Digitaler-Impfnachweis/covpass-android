package de.rki.covpass.checkapp.scanner

import android.os.Bundle
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.validation.*
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.utils.formatOrEmpty
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
internal class CovPassCheckHardwareScannerFragmentNav(
    val barcode: String
) : FragmentNav(CovPassCheckHardwareScannerFragment::class)

/**
 * Scanner Fragment extending from BaseFragment to intercept qr code scan result performed through the HW-scanner.
 */
internal class CovPassCheckHardwareScannerFragment() :
    BaseFragment(), DialogListener, CovPassCheckQRScannerEvents, ValidationResultListener {
    private val args: CovPassCheckHardwareScannerFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { CovPassCheckQRScannerViewModel(scope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onQrContentReceived(args.barcode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        findNavigator().pop()
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
        findNavigator().pop()
    }
}
