/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.scanner

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.validation.ValidationImmunityResultIncompleteFragmentNav
import de.rki.covpass.checkapp.validation.ValidationImmunityResultSuccessFragmentNav
import de.rki.covpass.checkapp.validation.ValidationPendingResultFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultDifferentDataFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultDifferentDataImmunityCheckFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultFailedFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultFailedSecondCertificateFragmentNav
import de.rki.covpass.checkapp.validation.ValidationResultListener
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
        initializeMediaPlayer(requireContext())
    }

    private fun initializeMediaPlayer(context: Context) {
        if (commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.value) {
            val mediaPath = Uri.parse(
                "android.resource://${context.packageName}/" +
                    "${R.raw.covpass_check_certificate_scanned}",
            )
            mp = MediaPlayer().apply {
                setDataSource(context, mediaPath)
                prepare()
            }
        }
    }

    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_camera_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_scan_camera_closing_announce
    override val isCovpass = false

    override fun onBarcodeResult(qrCode: String) {
        if (commonDeps.acousticFeedbackRepository.acousticFeedbackStatus.value) {
            mp.start()
        }
        viewModel.onQrContentReceived(qrCode)
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        scanEnabled.value = true
        when {
            tag == TAG_ERROR_DIFFERENT_DATA && action == DialogAction.NEGATIVE -> {
                viewModel.firstCovCertificate = null
                viewModel.secondCovCertificate = null
                viewModel.thirdCovCertificate = null
            }
            tag == TAG_ERROR_DUPLICATED_CERTIFICATES && action == DialogAction.NEGATIVE -> {
                findNavigator().popAll()
            }
        }
    }

    override fun onValidationSuccess(
        certificate: CovCertificate,
        isSecondCertificate: Boolean,
        dataComparison: DataComparison,
    ) {
        scanEnabled.value = false
        val firstCertificate = viewModel.firstCovCertificate
        val secondCertificate = viewModel.secondCovCertificate
        when {
            isSecondCertificate && firstCertificate != null && secondCertificate != null -> {
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
                    findNavigator().push(
                        ValidationResultDifferentDataFragmentNav(
                            firstCertificate.fullName,
                            firstCertificate.fullTransliteratedName,
                            firstCertificate.birthDate,
                            secondCertificate.fullName,
                            secondCertificate.fullTransliteratedName,
                            secondCertificate.birthDate,
                            dateDifferent = dataComparison == DataComparison.DateOfBirthDifferent,
                            expertModeData = certificate.getExpertModeData(),
                            isGermanCertificate = certificate.isGermanCertificate,
                            isSuccess = true,
                        ),
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

    override fun onValidationFailure(
        certificate: CovCertificate,
        isSecondCertificate: Boolean,
        dataComparison: DataComparison,
    ) {
        val firstCertificate = viewModel.firstCovCertificate
        val secondCertificate = viewModel.secondCovCertificate
        scanEnabled.value = false
        if (dataComparison == DataComparison.Equal || secondCertificate == null || firstCertificate == null) {
            findNavigator().push(
                ValidationResultPartialFragmentNav(
                    expertModeData = certificate.getExpertModeData(),
                    isGermanCertificate = certificate.isGermanCertificate,
                    allowSecondCertificate = !isSecondCertificate,
                ),
            )
        } else {
            findNavigator().push(
                ValidationResultDifferentDataFragmentNav(
                    firstCertificate.fullName,
                    firstCertificate.fullTransliteratedName,
                    firstCertificate.birthDate,
                    secondCertificate.fullName,
                    secondCertificate.fullTransliteratedName,
                    secondCertificate.birthDate,
                    dateDifferent = dataComparison == DataComparison.DateOfBirthDifferent,
                    expertModeData = certificate.getExpertModeData(),
                    isGermanCertificate = certificate.isGermanCertificate,
                ),
            )
        }
    }

    override fun onValidationTechnicalFailure(
        certificate: CovCertificate?,
        numberOfCertificates: Int,
    ) {
        scanEnabled.value = false
        if (numberOfCertificates > 1) {
            findNavigator().push(
                ValidationResultFailedSecondCertificateFragmentNav(
                    expertModeData = certificate?.getExpertModeData(),
                    isGermanCertificate = certificate?.isGermanCertificate ?: false,
                ),
            )
        } else {
            findNavigator().push(
                ValidationResultFailedFragmentNav(
                    expertModeData = certificate?.getExpertModeData(),
                    isGermanCertificate = certificate?.isGermanCertificate ?: false,
                ),
            )
        }
    }

    override fun onImmunityValidationSuccess(
        certificate: CovCertificate,
        dataComparison: DataComparison3Certs,
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
        numberOfCertificates: Int,
    ) {
        scanEnabled.value = false
        if (
            dataComparison == DataComparison3Certs.Equal ||
            (numberOfCertificates == 3 && dataComparison == DataComparison3Certs.SecondDifferentName) ||
            firstCovCert == null
        ) {
            findNavigator().push(
                ValidationImmunityResultSuccessFragmentNav(
                    name = certificate.fullName,
                    transliteratedName = certificate.fullTransliteratedName,
                    birthDate = formatDateFromString(certificate.birthDateFormatted),
                    expertModeData = certificate.getExpertModeData(),
                    isGermanCertificate = certificate.isGermanCertificate,
                ),
            )
        } else {
            onImmunityValidationDataDifference(
                certificate,
                dataComparison,
                firstCovCert,
                secondCovCert,
                numberOfCertificates,
                false,
            )
        }
    }

    override fun onImmunityValidationFailure(
        certificate: CovCertificate,
        dataComparison: DataComparison3Certs,
        firstCovCert: CovCertificate?,
        secondCovCert: CovCertificate?,
        numberOfCertificates: Int,
    ) {
        scanEnabled.value = false
        if (
            dataComparison == DataComparison3Certs.Equal ||
            (numberOfCertificates == 3 && dataComparison == DataComparison3Certs.SecondDifferentName) ||
            firstCovCert == null
        ) {
            if (numberOfCertificates < 3) {
                findNavigator().push(
                    ValidationPendingResultFragmentNav(
                        certificate.getExpertModeData(),
                        certificate.isGermanCertificate,
                        numberOfCertificates,
                    ),
                )
            } else {
                findNavigator().push(
                    ValidationImmunityResultIncompleteFragmentNav(
                        certificate.getExpertModeData(),
                        certificate.isGermanCertificate,
                    ),
                )
            }
        } else {
            onImmunityValidationDataDifference(
                certificate,
                dataComparison,
                firstCovCert,
                secondCovCert,
                numberOfCertificates,
                true,
            )
        }
    }

    private fun onImmunityValidationDataDifference(
        certificate: CovCertificate,
        dataComparison: DataComparison3Certs,
        firstCovCert: CovCertificate,
        secondCovCert: CovCertificate?,
        numberOfCertificates: Int,
        isPendingStatus: Boolean,
    ) {
        findNavigator().push(
            ValidationResultDifferentDataImmunityCheckFragmentNav(
                name1 = firstCovCert.fullName,
                transliteratedName1 = firstCovCert.fullTransliteratedName,
                birthDate1 = formatDateFromString(firstCovCert.birthDateFormatted),
                name2 = secondCovCert?.fullName ?: certificate.fullName,
                transliteratedName2 = secondCovCert?.fullTransliteratedName
                    ?: certificate.fullTransliteratedName,
                birthDate2 = if (secondCovCert != null) {
                    formatDateFromString(secondCovCert.birthDateFormatted)
                } else {
                    formatDateFromString(certificate.birthDateFormatted)
                },
                name3 = if (secondCovCert != null) {
                    certificate.fullName
                } else {
                    null
                },
                transliteratedName3 = if (secondCovCert != null) {
                    certificate.fullTransliteratedName
                } else {
                    null
                },
                birthDate3 = if (secondCovCert != null) {
                    formatDateFromString(certificate.birthDateFormatted)
                } else {
                    null
                },
                dataComparison3Certs = dataComparison,
                isPendingStatus = isPendingStatus,
                numberOfCertificates = numberOfCertificates,
                expertModeData = certificate.getExpertModeData(),
                isGermanCertificate = certificate.isGermanCertificate,
            ),
        )
    }

    override fun onImmunityValidationTechnicalFailure(
        certificate: CovCertificate?,
        numberOfCertificates: Int,
    ) {
        scanEnabled.value = false
        if (numberOfCertificates > 1) {
            findNavigator().push(
                ValidationResultFailedSecondCertificateFragmentNav(
                    expertModeData = certificate?.getExpertModeData(),
                    isGermanCertificate = certificate?.isGermanCertificate ?: false,
                ),
            )
        } else {
            findNavigator().push(
                ValidationResultFailedFragmentNav(
                    expertModeData = certificate?.getExpertModeData(),
                    isGermanCertificate = certificate?.isGermanCertificate ?: false,
                ),
            )
        }
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

    override fun showWarningDifferentData() {
        val dialog = DialogModel(
            titleRes = R.string.infschg_name_matching_error_title,
            messageString = getString(R.string.infschg_name_matching_error_copy),
            positiveButtonTextRes = R.string.infschg_name_matching_error_retry,
            negativeButtonTextRes = R.string.infschg_name_matching_error_cancel,
            tag = TAG_ERROR_DIFFERENT_DATA,
        )
        showDialog(dialog, childFragmentManager)
    }

    override fun showWarningDuplicatedCertificate() {
        val dialog = DialogModel(
            titleRes = R.string.dialog_second_scan_title,
            messageString = getString(R.string.dialog_second_scan_message),
            positiveButtonTextRes = R.string.dialog_second_scan_button_repeat,
            negativeButtonTextRes = R.string.dialog_second_scan_button_update,
            tag = TAG_ERROR_DUPLICATED_CERTIFICATES,
        )
        showDialog(dialog, childFragmentManager)
    }

    override fun onValidationFirstScanFinish() {
        scanEnabled.value = true
        viewModel.secondCovCertificate = null
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    override fun onValidationResultClosed() {
        scanEnabled.value = true
        viewModel.firstCovCertificate = null
        viewModel.secondCovCertificate = null
        viewModel.thirdCovCertificate = null
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    override fun onValidationContinueToNextScan() {
        scanEnabled.value = true
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    override fun onValidationRetryLastScan() {
        scanEnabled.value = true
        if (viewModel.thirdCovCertificate != null) {
            viewModel.thirdCovCertificate = null
        } else {
            viewModel.secondCovCertificate = null
        }
        sendAccessibilityAnnouncementEvent(announcementAccessibilityRes)
    }

    private fun CovCertificate.getExpertModeData(): ExpertModeData? {
        return if (kid.isNotEmpty() && rValue.isNotEmpty()) {
            ExpertModeData(
                transactionNumber =
                "$kid${getRValueByteArray.toHex()}${System.currentTimeMillis() / 1000}".sha256()
                    .toHex().trim(),
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
                ValidationPendingResultFragmentNav(
                    viewModel.firstCovCertificate?.getExpertModeData(),
                    viewModel.firstCovCertificate?.isGermanCertificate ?: false,
                    listOfNotNull(
                        viewModel.firstCovCertificate,
                        viewModel.secondCovCertificate,
                        viewModel.thirdCovCertificate,
                    ).size,
                ),
            )
        } else {
            return super.onBackPressed()
        }
        return Abort
    }

    private companion object {
        const val TAG_ERROR_UNEXPECTED_TYPE = "tag_error_unexpected_type"
        const val TAG_ERROR_DIFFERENT_DATA = "tag_error_different_data"
        const val TAG_ERROR_DUPLICATED_CERTIFICATES = "tag_error_duplicated_certificates"
    }
}
