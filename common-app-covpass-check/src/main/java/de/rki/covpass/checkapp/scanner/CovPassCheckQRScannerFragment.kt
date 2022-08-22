/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.dependencies.covpassCheckDeps
import de.rki.covpass.checkapp.validation.ValidAntigenTestFragmentNav
import de.rki.covpass.checkapp.validation.ValidPcrTestFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResult2GListener
import de.rki.covpass.checkapp.validation.ValidationResult2gDifferentDataFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResult2gFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResult2gPlusBBoosterFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultFailureFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultListener
import de.rki.covpass.checkapp.validation.ValidationResultSuccessNav
import de.rki.covpass.checkapp.validation.ValidationResultTechnicalFailureFragmentNav
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.errorhandling.CommonErrorHandler.Companion.ERROR_CODE_QR_CODE_DUPLICATED
import de.rki.covpass.commonapp.scanner.QRScannerFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.ExpertModeData
import de.rki.covpass.sdk.utils.DataComparison
import de.rki.covpass.sdk.utils.formatDateFromString
import de.rki.covpass.sdk.utils.sha256
import de.rki.covpass.sdk.utils.toHex
import de.rki.covpass.sdk.utils.toISO8601orEmpty
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
internal class CovPassCheckQRScannerFragmentNav : FragmentNav(CovPassCheckQRScannerFragment::class)

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

    private val isTwoGPlusOn by lazy {
        covpassCheckDeps.checkAppRepository.is2GPlusOn() || covpassCheckDeps.checkAppRepository.is2GPlusBOn()
    }
    private val isTwoGPlusBOn by lazy { covpassCheckDeps.checkAppRepository.is2GPlusBOn() }
    private val viewModel by reactiveState { CovPassCheckQRScannerViewModel(scope) }
    private val dataViewModel by reactiveState {
        CovPassCheckQRScannerDataViewModel(
            scope,
            isTwoGPlusOn,
            isTwoGPlusBOn,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifyRecoveryOlder90DaysIsValid()
    }

    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_camera_announce
    override val isCovpass = false

    override fun onBarcodeResult(qrCode: String) {
        viewModel.onQrContentReceived(qrCode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (
            tag == TAG_ERROR_2G_UNEXPECTED_TYPE &&
            (
                dataViewModel.firstCertificateData2G != null ||
                    dataViewModel.secondCertificateData2G != null
                )
        ) {
            dataViewModel.firstCertificateData2G?.let { firstCertificateData2G ->
                findNavigator().push(
                    ValidationResult2gFragmentNav(
                        firstCertificateData2G,
                        dataViewModel.secondCertificateData2G,
                    ),
                )
            }
        } else {
            scanEnabled.value = true
        }
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
        verifyRecoveryOlder90DaysIsValid()
    }

    override fun onValidatingFirstCertificate(
        firstCertificateData: ValidationResult2gData?,
    ) {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
        dataViewModel.firstCertificateData2G = firstCertificateData
        dataViewModel.secondCertificateData2G = null
        verifyRecoveryOlder90DaysIsValid()
    }

    override fun on2gData(
        firstCertData: ValidationResult2gData?,
        secondCertData: ValidationResult2gData?,
    ) {
        if (firstCertData == null) return
        if (secondCertData == null) {
            findNavigator().push(
                ValidationResult2gFragmentNav(
                    firstCertData,
                    secondCertData,
                ),
            )
            return
        }

        findNavigator().push(
            when (dataViewModel.compareData(firstCertData, secondCertData)) {
                DataComparison.Equal, DataComparison.HasNullData, DataComparison.HasInvalidData -> {
                    ValidationResult2gFragmentNav(
                        firstCertData,
                        secondCertData,
                    )
                }
                DataComparison.NameDifferent -> {
                    ValidationResult2gDifferentDataFragmentNav(
                        firstCertData,
                        secondCertData,
                        false,
                    )
                }
                DataComparison.DateOfBirthDifferent -> {
                    ValidationResult2gDifferentDataFragmentNav(
                        firstCertData,
                        secondCertData,
                        true,
                    )
                }
            },
        )
    }

    override fun on2gPlusBData(boosterCertData: ValidationResult2gData) {
        findNavigator().push(
            ValidationResult2gPlusBBoosterFragmentNav(
                boosterCertData,
            ),
        )
    }

    private fun CovCertificate.getExpertModeData(): ExpertModeData? {
        return if (kid.isNotEmpty() && rValue.isNotEmpty()) {
            ExpertModeData(
                transactionNumber =
                "$kid${getRValueByteArray.toHex()}${System.currentTimeMillis() / 1000}".sha256().toHex().trim(),
                kid = kid,
                rValueSignature = getRValueByteArray.toHex(),
                issuingCountry = issuer.uppercase(),
                dateOfIssue = validFrom.toISO8601orEmpty(),
                technicalExpiryDate = validUntil.toISO8601orEmpty(),
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
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
            ),
        )
    }

    override fun on3gValidPcrTest(certificate: CovCertificate, sampleCollection: ZonedDateTime?) {
        findNavigator().push(
            ValidPcrTestFragmentNav(
                name = certificate.fullName,
                transliteratedName = certificate.fullTransliteratedName,
                birthDate = formatDateFromString(certificate.birthDateFormatted),
                sampleCollection = sampleCollection,
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
            ),
        )
    }

    override fun on3gValidAntigenTest(
        certificate: CovCertificate,
        sampleCollection: ZonedDateTime?,
    ) {
        findNavigator().push(
            ValidAntigenTestFragmentNav(
                name = certificate.fullName,
                transliteratedName = certificate.fullTransliteratedName,
                birthDate = formatDateFromString(certificate.birthDateFormatted),
                sampleCollection = sampleCollection,
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
            ),
        )
    }

    override fun on3gTechnicalFailure(is2gOn: Boolean) {
        findNavigator().push(ValidationResultTechnicalFailureFragmentNav(is2gOn))
    }

    override fun on3gFailure(certificate: CovCertificate?, is2gOn: Boolean) {
        findNavigator().push(
            ValidationResultFailureFragmentNav(
                is2gOn = is2gOn,
                expertModeData = certificate?.getExpertModeData(),
                isGermanCertificate = certificate?.isGermanCertificate == true,
            ),
        )
    }

    override fun showWarning2gUnexpectedType() {
        val dialog = DialogModel(
            titleRes = R.string.error_2G_unexpected_type_title,
            messageString = "${getString(R.string.error_2G_unexpected_type_copy)} (Error " +
                "$ERROR_CODE_QR_CODE_DUPLICATED)",
            positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
            tag = TAG_ERROR_2G_UNEXPECTED_TYPE,
        )
        showDialog(dialog, childFragmentManager)
    }

    override fun showLoading(isLoading: Boolean) {
        super.showLoading(isLoading)
        if (!isLoading) {
            checkPermission()
        }
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
                        dataViewModel.secondCertificateData2G,
                    ),
                )
            }
        } else {
            return super.onBackPressed()
        }
        return Abort
    }

    private fun verifyRecoveryOlder90DaysIsValid() {
        viewModel.recoveryOlder90DaysValid.value = isTwoGPlusOn &&
            dataViewModel.secondCertificateData2G == null &&
            (
                dataViewModel.firstCertificateData2G == null ||
                    dataViewModel.firstCertificateData2G?.isVaccination() == true ||
                    dataViewModel.firstCertificateData2G?.isBooster() == true
                )
    }

    private companion object {
        const val TAG_ERROR_2G_UNEXPECTED_TYPE = "tag_error_2g_unexpected_type"
    }
}
