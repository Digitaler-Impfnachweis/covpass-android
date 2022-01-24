/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.validation.*
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler.Companion.ERROR_CODE_QR_CODE_DUPLICATED
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.utils.formatDateFromString
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
internal class CovPassCheckQRScannerFragmentNav(
    val isTwoGOn: Boolean,
    val isTwoGPlusBOn: Boolean,
) : FragmentNav(CovPassCheckQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassCheckQRScannerFragment :
    QRScannerFragment(),
    DialogListener,
    CovPassCheckQRScannerEvents,
    ValidationResultListener,
    ValidationResult2GListener,
    CovPassCheckQRScannerDataEvents {

    private val isTwoGOn by lazy { getArgs<CovPassCheckQRScannerFragmentNav>().isTwoGOn }
    private val isTwoGPlusBOn by lazy { getArgs<CovPassCheckQRScannerFragmentNav>().isTwoGPlusBOn }
    private val viewModel by reactiveState { CovPassCheckQRScannerViewModel(scope) }
    private val dataViewModel by reactiveState { CovPassCheckQRScannerDataViewModel(scope, isTwoGOn, isTwoGPlusBOn) }

    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_camera_announce

    override fun onBarcodeResult(qrCode: String) {
        viewModel.onQrContentReceived(qrCode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onValidationSuccess(certificate: CovCertificate) {
        scanEnabled.value = false
        dataViewModel.prepareDataOnSuccess(certificate)
    }

    override fun onValidPcrTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?,
    ) {
        scanEnabled.value = false
        dataViewModel.prepareDataOnValidPcrTest(certificate, sampleCollection)
    }

    override fun onValidAntigenTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?,
    ) {
        scanEnabled.value = false
        dataViewModel.prepareDataOnValidAntigenTest(certificate, sampleCollection)
    }

    override fun onValidationFailure(isTechnical: Boolean, certificate: CovCertificate?) {
        scanEnabled.value = false
        dataViewModel.prepareDataOnValidationFailure(isTechnical, certificate)
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    override fun onValidationResetOrFinish() {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
        dataViewModel.certificateData2G = null
        dataViewModel.testCertificateData2G = null
    }

    override fun onValidatingFirstCertificate(
        certificateData: ValidationResult2gData?,
        testCertificateData: ValidationResult2gData?,
    ) {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
        dataViewModel.certificateData2G = certificateData
        dataViewModel.testCertificateData2G = testCertificateData
    }

    override fun on2gData(certData: ValidationResult2gData?, testData: ValidationResult2gData?, certFirst: Boolean) {
        findNavigator().push(
            when (dataViewModel.compareData(certData, testData)) {
                DataComparison.Equal, DataComparison.HasNullData -> {
                    ValidationResult2gFragmentNav(
                        certData,
                        testData
                    )
                }
                DataComparison.NameDifferent -> {
                    if (certData != null && testData != null) {
                        ValidationResult2gDifferentDataFragmentNav(
                            certData,
                            testData,
                            certFirst,
                            false
                        )
                    } else {
                        ValidationResult2gFragmentNav(
                            certData,
                            testData
                        )
                    }
                }
                DataComparison.DateOfBirthDifferent -> {
                    if (certData != null && testData != null) {
                        ValidationResult2gDifferentDataFragmentNav(
                            certData,
                            testData,
                            certFirst,
                            true
                        )
                    } else {
                        ValidationResult2gFragmentNav(
                            certData,
                            testData
                        )
                    }
                }
                DataComparison.IsBoosterInTwoGPlusB -> {
                    ValidationResult2gPlusBBoosterFragmentNav(
                        certData,
                    )
                }
            }
        )
    }

    override fun on3gSuccess(certificate: CovCertificate) {
        findNavigator().push(
            ValidationResultSuccessNav(
                certificate.fullName,
                certificate.fullTransliteratedName,
                formatDateFromString(certificate.birthDateFormatted)
            )
        )
    }

    override fun on3gValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        findNavigator().push(
            ValidPcrTestFragmentNav(
                certificate.fullName,
                formatDateFromString(certificate.birthDateFormatted),
                certificate.fullTransliteratedName,
                sampleCollection
            )
        )
    }

    override fun on3gValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        findNavigator().push(
            ValidAntigenTestFragmentNav(
                certificate.fullName,
                certificate.fullTransliteratedName,
                formatDateFromString(certificate.birthDateFormatted),
                sampleCollection
            )
        )
    }

    override fun on3gTechnicalFailure(is2gOn: Boolean) {
        findNavigator().push(ValidationResultTechnicalFailureFragmentNav(is2gOn))
    }

    override fun on3gFailure(is2gOn: Boolean) {
        findNavigator().push(ValidationResultFailureFragmentNav(is2gOn))
    }

    override fun showWarning2gUnexpectedType() {
        val dialog = DialogModel(
            titleRes = R.string.error_2G_unexpected_type_title,
            messageString = "${getString(R.string.error_2G_unexpected_type_copy)} (Error " +
                "$ERROR_CODE_QR_CODE_DUPLICATED)",
            positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
            tag = TAG_ERROR_2G_UNEXPECTED_TYPE
        )
        showDialog(dialog, childFragmentManager)
    }

    override fun onBackPressed(): Abortable {
        if (
            dataViewModel.certificateData2G != null ||
            dataViewModel.testCertificateData2G != null
        ) {
            findNavigator().push(
                ValidationResult2gFragmentNav(
                    dataViewModel.certificateData2G,
                    dataViewModel.testCertificateData2G
                )
            )
        } else {
            return super.onBackPressed()
        }
        return Abort
    }

    private companion object {
        const val TAG_ERROR_2G_UNEXPECTED_TYPE = "tag_error_2g_unexpected_type"
    }
}
