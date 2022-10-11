/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.validation.ValidationResultDifferentDataFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultInvalidFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultListener
import de.rki.covpass.checkapp.validation.ValidationResultNoRulesFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultPartialFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultSuccessFragmentNav
import de.rki.covpass.commonapp.dependencies.commonDeps
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

@Parcelize
internal class CovPassCheckQRScannerFragmentNav : FragmentNav(CovPassCheckQRScannerFragment::class)

/**
 * QR Scanner Fragment extending from QRScannerFragment to intercept qr code scan result.
 */
internal class CovPassCheckQRScannerFragment :
    QRScannerFragment(),
    DialogListener,
    CovPassCheckQRScannerEvents,
    ValidationResultListener {

    private val viewModel by reactiveState { CovPassCheckQRScannerViewModel(scope) }
    private lateinit var mp: MediaPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mp = MediaPlayer.create(requireContext(), R.raw.covpass_check_certificate_scanned)
    }

    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_camera_announce
    override val isCovpass = false

    override fun onBarcodeResult(qrCode: String) {
        if (commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.value) {
            mp.start()
        }
        viewModel.onQrContentReceived(qrCode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
    }

    override fun onValidationSuccess(
        certificate: CovCertificate,
        isSecondCertificate: Boolean,
        dataComparison: DataComparison,
    ) {
        scanEnabled.value = false
        val firstCertificate = viewModel.firstCovCertificate
        when {
            isSecondCertificate && firstCertificate != null -> {
                if (dataComparison == DataComparison.Equal) {
                    findNavigator().push(
                        ValidationResultSuccessFragmentNav(
                            name = certificate.fullName,
                            transliteratedName = certificate.fullTransliteratedName,
                            birthDate = formatDateFromString(certificate.birthDateFormatted),
                            expertModeData = certificate.getExpertModeData(),
                            isGermanCertificate = certificate.isGermanCertificate,
                        ),
                    )
                } else {
                    ValidationResultDifferentDataFragmentNav(
                        firstCertificate.fullName,
                        firstCertificate.fullTransliteratedName,
                        firstCertificate.birthDate,
                        certificate.fullName,
                        certificate.fullTransliteratedName,
                        certificate.birthDate,
                        dateDifferent = dataComparison == DataComparison.DateOfBirthDifferent,
                        expertModeData = certificate.getExpertModeData(),
                        isGermanCertificate = certificate.isGermanCertificate,
                    )
                }
            }
            else -> {
                findNavigator().push(
                    ValidationResultSuccessFragmentNav(
                        name = certificate.fullName,
                        transliteratedName = certificate.fullTransliteratedName,
                        birthDate = formatDateFromString(certificate.birthDateFormatted),
                        expertModeData = certificate.getExpertModeData(),
                        isGermanCertificate = certificate.isGermanCertificate,
                    ),
                )
            }
        }
    }

    override fun onValidationFailure(certificate: CovCertificate, isSecondCertificate: Boolean) {
        scanEnabled.value = false
        findNavigator().push(
            ValidationResultPartialFragmentNav(
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
                allowSecondCertificate = !isSecondCertificate,
            ),
        )
    }

    override fun onValidationTechnicalFailure(certificate: CovCertificate?) {
        scanEnabled.value = false
        findNavigator().push(
            ValidationResultInvalidFragmentNav(
                expertModeData = certificate?.getExpertModeData(),
                isGermanCertificate = certificate?.isGermanCertificate ?: false,
            ),
        )
    }

    override fun onValidationNoRulesFailure(certificate: CovCertificate) {
        scanEnabled.value = false
        findNavigator().push(
            ValidationResultNoRulesFragmentNav(
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
            ),
        )
    }

    override fun showWarningDuplicatedType() {
        val dialog = DialogModel(
            titleRes = R.string.error_2G_unexpected_type_title,
            messageString = "${getString(R.string.error_2G_unexpected_type_copy)} (Error " +
                "$ERROR_CODE_QR_CODE_DUPLICATED)",
            positiveButtonTextRes = R.string.error_scan_qrcode_cannot_be_parsed_button_title,
            tag = TAG_ERROR_UNEXPECTED_TYPE,
        )
        showDialog(dialog, childFragmentManager)
    }

    override fun onValidationFirstScanFinish() {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
        viewModel.firstCovCertificate = null
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
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

    override fun showLoading(isLoading: Boolean) {
        super.showLoading(isLoading)
        if (!isLoading) {
            checkPermission()
        }
    }

    override fun onBackPressed(): Abortable {
        val previousCovCertificate = viewModel.firstCovCertificate
        if (previousCovCertificate != null) {
            scanEnabled.value = false
            findNavigator().push(
                ValidationResultPartialFragmentNav(
                    expertModeData = previousCovCertificate.getExpertModeData(),
                    isGermanCertificate = previousCovCertificate.isGermanCertificate,
                    allowSecondCertificate = true,
                ),
            )
        } else {
            return super.onBackPressed()
        }
        return Abort
    }

    private companion object {
        const val TAG_ERROR_UNEXPECTED_TYPE = "tag_error_unexpected_type"
    }
}
