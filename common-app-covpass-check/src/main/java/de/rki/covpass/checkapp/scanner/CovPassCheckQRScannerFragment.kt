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
import de.rki.covpass.checkapp.revocation.CovPassCheckCountryResolver
import de.rki.covpass.checkapp.validation.*
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler.Companion.ERROR_CODE_QR_CODE_DUPLICATED
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.ExpertModeData
import de.rki.covpass.sdk.utils.*
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
        sampleCollection: ZonedDateTime?
    ) {
        scanEnabled.value = false
        dataViewModel.prepareDataOnValidPcrTest(certificate, sampleCollection)
    }

    override fun onValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
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
        dataViewModel.firstCertificateData2G = null
        dataViewModel.secondCertificateData2G = null
    }

    override fun onValidatingFirstCertificate(
        firstCertificateData: ValidationResult2gData?,
    ) {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
        dataViewModel.firstCertificateData2G = firstCertificateData
        dataViewModel.secondCertificateData2G = null
    }

    override fun on2gData(firstCertData: ValidationResult2gData?, secondCertData: ValidationResult2gData?) {
        if (firstCertData == null) return
        if (secondCertData == null) {
            findNavigator().push(
                ValidationResult2gFragmentNav(
                    firstCertData,
                    secondCertData
                )
            )
            return
        }

        findNavigator().push(
            when (dataViewModel.compareData(firstCertData, secondCertData)) {
                DataComparison.Equal, DataComparison.HasNullData -> {
                    ValidationResult2gFragmentNav(
                        firstCertData,
                        secondCertData
                    )
                }
                DataComparison.NameDifferent -> {
                    ValidationResult2gDifferentDataFragmentNav(
                        firstCertData,
                        secondCertData,
                        false
                    )
                }
                DataComparison.DateOfBirthDifferent -> {
                    ValidationResult2gDifferentDataFragmentNav(
                        firstCertData,
                        secondCertData,
                        true
                    )
                }
            }
        )
    }

    override fun on2gPlusBData(boosterCertData: ValidationResult2gData) {
        findNavigator().push(
            ValidationResult2gPlusBBoosterFragmentNav(
                boosterCertData
            )
        )
    }

    private fun CovCertificate.getExpertModeData(): ExpertModeData? {
        return if (kid.isNotEmpty() && rValue.isNotEmpty()) {
            ExpertModeData(
                "$kid${rValue.toHex()}${System.currentTimeMillis() / 1000}".sha256().toHex().trim(),
                kid,
                rValue.toHex(),
                CovPassCheckCountryResolver.getCountryLocalized(issuer),
                validFrom.formatDateDeOrEmpty(),
                validUntil.formatDateDeOrEmpty()
            )
        } else {
            null
        }
    }

    override fun on3gSuccess(certificate: CovCertificate) {
        findNavigator().push(
            ValidationResultSuccessNav(
                name = certificate.fullName,
                transliteratedName = certificate.fullTransliteratedName,
                birthDate = formatDateFromString(certificate.birthDateFormatted),
                expertModeData = certificate.getExpertModeData()
            )
        )
    }

    override fun on3gValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        findNavigator().push(
            ValidPcrTestFragmentNav(
                name = certificate.fullName,
                transliteratedName = certificate.fullTransliteratedName,
                birthDate = formatDateFromString(certificate.birthDateFormatted),
                sampleCollection = sampleCollection,
                expertModeData = certificate.getExpertModeData()
            )
        )
    }

    override fun on3gValidAntigenTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        findNavigator().push(
            ValidAntigenTestFragmentNav(
                name = certificate.fullName,
                transliteratedName = certificate.fullTransliteratedName,
                birthDate = formatDateFromString(certificate.birthDateFormatted),
                sampleCollection = sampleCollection,
                expertModeData = certificate.getExpertModeData()
            )
        )
    }

    override fun on3gTechnicalFailure(is2gOn: Boolean) {
        findNavigator().push(ValidationResultTechnicalFailureFragmentNav(is2gOn))
    }

    override fun on3gFailure(certificate: CovCertificate?, is2gOn: Boolean) {
        findNavigator().push(
            ValidationResultFailureFragmentNav(
                is2gOn,
                certificate?.getExpertModeData()
            )
        )
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
            dataViewModel.firstCertificateData2G != null ||
            dataViewModel.secondCertificateData2G != null
        ) {
            scanEnabled.value = false
            dataViewModel.firstCertificateData2G?.let { firstCertificateData2G ->
                findNavigator().push(
                    ValidationResult2gFragmentNav(
                        firstCertificateData2G,
                        dataViewModel.secondCertificateData2G
                    )
                )
            }
        } else {
            return super.onBackPressed()
        }
        return Abort
    }

    private companion object {
        const val TAG_ERROR_2G_UNEXPECTED_TYPE = "tag_error_2g_unexpected_type"
    }
}
