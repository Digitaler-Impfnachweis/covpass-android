/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ValidationResult2gBinding
import de.rki.covpass.checkapp.scanner.ValidationResult2gData
import de.rki.covpass.checkapp.uielements.ValidationResult2gCertificateElement
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.utils.daysTillNow
import de.rki.covpass.sdk.utils.hoursTillNow
import de.rki.covpass.sdk.utils.monthTillNow
import kotlinx.parcelize.Parcelize

internal interface ValidationResult2GListener {
    fun onValidationResetOrFinish()
    fun onValidatingFirstCertificate(
        firstCertificateData: ValidationResult2gData?
    )
}

@Parcelize
public class ValidationResult2gFragmentNav(
    public val firstCertificateData: ValidationResult2gData,
    public val secondCertificateData: ValidationResult2gData?,
) : FragmentNav(ValidationResult2gFragment::class)

public class ValidationResult2gFragment : BaseBottomSheet(), ValidationResultListener {

    private val args by lazy { getArgs<ValidationResult2gFragmentNav>() }
    private val firstCertificateData by lazy { args.firstCertificateData }
    private val secondCertificateData by lazy { args.secondCertificateData }
    private val binding by viewBinding(ValidationResult2gBinding::inflate)

    override val buttonTextRes: Int by lazy { textForActivationButton() }
    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT
    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_result_announce_2G
    // TODO add accessibility closing message accessibility_scan_result_closing_announce_2G

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareView()
    }

    private fun prepareView() {
        bottomSheetBinding.bottomSheetTitle.text = getString(R.string.result_2G_title)

        fillCertificateElements(binding.validationResultCertificate, firstCertificateData, secondCertificateData)

        if (secondCertificateData != null) {
            secondCertificateData?.let { secondCertificateData ->
                fillCertificateElements(
                    binding.validationResultSecondCertificate,
                    secondCertificateData,
                    firstCertificateData
                )
            }
        } else {
            fillEmptyElement()
        }

        fillDataElement()

        binding.validationResultTypeText.setText(R.string.result_2G__3rd_footnote)
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = !(
                secondCertificateData != null &&
                    firstCertificateData.isValid() &&
                    secondCertificateData?.isValid() == true
                ) &&
                !(secondCertificateData == null && firstCertificateData.isInvalid())
            setOnClickListener {
                findNavigator().popUntil<ValidationResult2GListener>()?.onValidationResetOrFinish()
            }
            setText(R.string.result_2G_button_startover)
        }
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible =
            bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.isVisible
        bottomSheetBinding.bottomSheetActionButton.isVisible = true
    }

    private fun fillCertificateElements(
        validationResultCertificate: ValidationResult2gCertificateElement,
        certificateData: ValidationResult2gData,
        secondaryCertificateData: ValidationResult2gData?,
    ) {
        when (certificateData.certificateResult) {
            CovPassCheckValidationResult.TechnicalError -> {
                validationResultCertificate.showInvalidCertificate(
                    R.drawable.validation_result_2g_invalid_certificate,
                    R.string.result_2G_certificate_invalid,
                ) {
                    findNavigator().push(ValidationResultTechnicalFailure2gFragmentNav())
                }
            }
            CovPassCheckValidationResult.ValidationError -> {
                validationResultCertificate.showInvalidCertificate(
                    R.drawable.validation_result_2g_invalid_certificate,
                    R.string.result_2G_certificate_invalid,
                ) {
                    findNavigator().push(ValidationResultFailure2gFragmentNav())
                }
            }
            CovPassCheckValidationResult.Success -> {
                validationResultCertificate.showValidCertificate(
                    when {
                        certificateData.isRecoveryOlder90Days && secondaryCertificateData == null -> {
                            R.drawable.validation_result_2g_recovery_older_90
                        }
                        certificateData.isRecoveryOlder90Days && secondaryCertificateData != null &&
                            secondaryCertificateData.isInvalid() -> {
                            R.drawable.validation_result_2g_recovery_older_90
                        }
                        certificateData.isTest() -> {
                            R.drawable.validation_result_2g_valid_test
                        }
                        else -> {
                            R.drawable.validation_result_2g_valid_certificate
                        }
                    },
                    getString(
                        when {
                            certificateData.isBooster() -> {
                                R.string.result_2G_2nd_booster_valid
                            }
                            certificateData.isVaccination() -> {
                                R.string.result_2G_2nd_basic_valid
                            }
                            certificateData.isRecovery() -> {
                                R.string.result_2G_2nd_recovery_valid
                            }
                            certificateData.isPCRTest() -> {
                                R.string.result_2G_2nd_pcrtest_valid
                            }
                            else -> {
                                R.string.result_2G_2nd_rapidtest_valid
                            }
                        }
                    ),
                    when {
                        certificateData.isBooster() -> {
                            getString(
                                R.string.result_2G_2nd_timestamp_days,
                                certificateData.validFrom?.daysTillNow()
                            )
                        }
                        certificateData.isTest() -> {
                            getString(
                                R.string.result_2G_2nd_timestamp_hours,
                                certificateData.sampleCollection?.hoursTillNow()
                            )
                        }
                        else -> {
                            getString(
                                R.string.result_2G_2nd_timestamp_months,
                                certificateData.validFrom?.monthTillNow()
                            )
                        }
                    }
                )
            }
        }
    }

    private fun fillEmptyElement() {
        binding.validationResultTypeText.isVisible =
            !firstCertificateData.isVaccination() &&
            !firstCertificateData.isBooster() &&
            !firstCertificateData.isRecoveryOlder90Days
        when {
            firstCertificateData.isVaccination() || firstCertificateData.isBooster() -> {
                binding.validationResultSecondCertificate.showEmptyCertificate(
                    R.drawable.validation_result_2g_empty_certificate,
                    R.string.result_2G_3rd_test_recov_empty,
                    R.string.result_2G_2nd_empty,
                )
            }
            firstCertificateData.isRecovery() && firstCertificateData.isRecoveryOlder90Days -> {
                binding.validationResultSecondCertificate.showEmptyCertificate(
                    R.drawable.validation_result_2g_empty_certificate,
                    R.string.result_2G_2nd_basic_valid,
                    R.string.result_2G_2nd_empty,
                )
            }
            firstCertificateData.isRecovery() -> {
                binding.validationResultSecondCertificate.showEmptyCertificate(
                    R.drawable.validation_result_2g_empty_certificate,
                    R.string.result_2G_3rd_test_vacc_empty,
                    R.string.result_2G_2nd_empty,
                )
            }
            firstCertificateData.isTest() -> {
                binding.validationResultSecondCertificate.showEmptyCertificate(
                    R.drawable.validation_result_2g_empty_certificate,
                    R.string.result_2G_3rd_vacc_recov_empty,
                    R.string.result_2G_2nd_empty,
                )
            }
        }
    }

    private fun fillDataElement() {
        if (validateDataElement()) {
            binding.validationResultInfoText.isVisible = false
            binding.validationResultInfoElement.isVisible = false
        } else {
            binding.validationResultInfoText.isVisible = true
            binding.validationResultInfoElement.isVisible = true
            binding.validationResultInfoText.setText(R.string.validation_check_popup_valid_pcr_test_message)
            binding.validationResultInfoElement.showInfo(
                R.drawable.validation_result_2g_data,
                firstCertificateData.certificateName
                    ?: secondCertificateData?.certificateName,
                firstCertificateData.certificateTransliteratedName
                    ?: secondCertificateData?.certificateTransliteratedName,
                getString(
                    R.string.validation_check_popup_valid_vaccination_date_of_birth,
                    firstCertificateData.certificateBirthDate ?: secondCertificateData?.certificateBirthDate,
                )
            )
        }
    }

    private fun validateDataElement() = secondCertificateData == null && firstCertificateData.isInvalid()

    private fun textForActivationButton() =
        when {
            secondCertificateData == null && firstCertificateData.isValid() -> {
                R.string.result_2G__3rd_button
            }
            secondCertificateData == null && firstCertificateData.isInvalid() -> {
                R.string.result_2G_button_startover
            }
            secondCertificateData != null &&
                firstCertificateData.isValid() &&
                secondCertificateData?.isValid() == true -> {
                R.string.result_2G_button_startover
            }
            else -> {
                R.string.result_2G_button_retry
            }
        }

    override fun onActionButtonClicked() {
        when {
            !bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.isVisible -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidationResetOrFinish()
            }
            firstCertificateData.isInvalid() -> {
                findNavigator().popUntil<ValidationResult2GListener>()?.onValidationResetOrFinish()
            }
            secondCertificateData != null && secondCertificateData?.isInvalid() == true -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(firstCertificateData)
            }
            else -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(firstCertificateData)
            }
        }
    }

    override fun onValidationResultClosed() {}

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onBackPressed(): Abortable {
        when {
            firstCertificateData.isInvalid() -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidationResetOrFinish()
            }
            secondCertificateData != null && secondCertificateData?.isInvalid() == true -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(firstCertificateData)
            }
            else -> {
                findNavigator().popAll()
            }
        }
        return Abort
    }
}
