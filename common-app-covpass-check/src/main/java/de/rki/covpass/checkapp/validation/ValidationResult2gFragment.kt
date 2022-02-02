/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validation

import android.os.Bundle
import android.os.Parcelable
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
import de.rki.covpass.checkapp.validitycheck.CovPassCheckValidationResult
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.utils.daysTillNow
import de.rki.covpass.sdk.utils.hoursTillNow
import de.rki.covpass.sdk.utils.monthTillNow
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZonedDateTime

internal interface ValidationResult2GListener {
    fun onValidationResetOrFinish()
    fun onValidatingFirstCertificate(
        certificateData: ValidationResult2gData?,
        testCertificateData: ValidationResult2gData?,
    )
}

@Parcelize
public class ValidationResult2gFragmentNav(
    public val certificateData: ValidationResult2gData?,
    public val testCertificateData: ValidationResult2gData?,
) : FragmentNav(ValidationResult2gFragment::class)

public class ValidationResult2gFragment : BaseBottomSheet(), ValidationResultListener {

    private val args by lazy { getArgs<ValidationResult2gFragmentNav>() }
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

        fillCertificateElement()

        fillTestElement()

        fillDataElement()

        binding.validationResultTypeText.setText(R.string.result_2G_footnote)
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = !(
                args.testCertificateData != null && args.certificateData != null &&
                    args.certificateData?.certificateResult == CovPassCheckValidationResult.Success &&
                    args.testCertificateData?.certificateResult == CovPassCheckValidationResult.Success
                ) &&
                !(
                    args.testCertificateData == null && args.certificateData != null &&
                        args.certificateData?.certificateResult != CovPassCheckValidationResult.Success
                    ) &&
                !(
                    args.certificateData == null && args.testCertificateData != null &&
                        args.testCertificateData?.certificateResult != CovPassCheckValidationResult.Success
                    )

            setOnClickListener {
                findNavigator().popUntil<ValidationResult2GListener>()?.onValidationResetOrFinish()
            }
            setText(R.string.result_2G_button_startover)
        }
        bottomSheetBinding.bottomSheetActionButton.isVisible = true
    }

    private fun fillCertificateElement() {
        if (args.certificateData == null) {
            binding.validationResultCertificate.showEmptyCertificate(
                R.drawable.validation_result_2g_empty_certificate,
                R.string.result_2G_gproof_empty,
                R.string.result_2G_empty_subtitle,
            )
        } else {
            when (args.certificateData?.certificateResult) {
                CovPassCheckValidationResult.TechnicalError -> {
                    binding.validationResultCertificate.showInvalidCertificate(
                        R.drawable.validation_result_2g_invalid_certificate,
                        R.string.result_2G_gproof_invalid,
                    ) {
                        findNavigator().push(ValidationResultTechnicalFailure2gFragmentNav())
                    }
                }
                CovPassCheckValidationResult.ValidationError -> {
                    binding.validationResultCertificate.showInvalidCertificate(
                        R.drawable.validation_result_2g_invalid_certificate,
                        R.string.result_2G_gproof_invalid,
                    ) {
                        findNavigator().push(ValidationResultFailure2gFragmentNav())
                    }
                }
                CovPassCheckValidationResult.Success -> {
                    binding.validationResultCertificate.showValidCertificate(
                        R.drawable.validation_result_2g_valid_certificate,
                        getString(
                            when {
                                args.certificateData?.isBooster() == true -> {
                                    R.string.result_2G_2nd_booster_valid
                                }
                                args.certificateData?.isVaccination() == true -> {
                                    R.string.result_2G_2nd_basic_valid
                                }
                                else -> {
                                    R.string.result_2G_2nd_recovery_valid
                                }
                            }
                        ),
                        getString(
                            if (args.certificateData?.isBooster() == true) {
                                R.string.result_2G_2nd_timestamp_days
                            } else {
                                R.string.result_2G_2nd_timestamp_months
                            },
                            if (args.certificateData?.isBooster() == true) {
                                args.certificateData?.validFrom?.daysTillNow()
                            } else {
                                args.certificateData?.validFrom?.monthTillNow()
                            }
                        )
                    )
                }
            }
        }
    }

    private fun fillTestElement() {
        if (args.testCertificateData == null) {
            binding.validationResultTestCertificate.showEmptyCertificate(
                R.drawable.validation_result_2g_empty_test,
                R.string.result_2G_test_empty,
                R.string.result_2G_empty_subtitle,
            )
        } else {
            when (args.testCertificateData?.certificateResult) {
                CovPassCheckValidationResult.TechnicalError -> {
                    binding.validationResultTestCertificate.showInvalidCertificate(
                        R.drawable.validation_result_2g_invalid_certificate,
                        R.string.result_2G_test_invalid,
                    ) {
                        findNavigator().push(ValidationResultTechnicalFailure2gFragmentNav())
                    }
                }
                CovPassCheckValidationResult.ValidationError -> {
                    binding.validationResultTestCertificate.showInvalidCertificate(
                        R.drawable.validation_result_2g_invalid_certificate,
                        R.string.result_2G_test_invalid,
                    ) {
                        findNavigator().push(ValidationResultFailure2gFragmentNav())
                    }
                }
                CovPassCheckValidationResult.Success -> {
                    binding.validationResultTestCertificate.showValidCertificate(
                        R.drawable.validation_result_2g_valid_test,
                        getString(
                            if (args.certificateData?.isTestPCR() == true) {
                                R.string.result_2G_2nd_pcrtest_valid
                            } else {
                                R.string.result_2G_2nd_rapidtest_valid
                            }
                        ),
                        getString(
                            R.string.result_2G_2nd_timestamp_hours,
                            args.testCertificateData?.sampleCollection?.hoursTillNow()
                        )
                    )
                }
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
                args.certificateData?.certificateName
                    ?: args.testCertificateData?.certificateName,
                args.certificateData?.certificateTransliteratedName
                    ?: args.testCertificateData?.certificateTransliteratedName,
                getString(
                    R.string.validation_check_popup_valid_vaccination_date_of_birth,
                    args.certificateData?.certificateBirthDate ?: args.testCertificateData?.certificateBirthDate,
                )
            )
        }
    }

    private fun validateDataElement() = (
        args.certificateData == null &&
            args.testCertificateData?.certificateResult != CovPassCheckValidationResult.Success
        ) ||
        (
            args.testCertificateData == null &&
                args.certificateData?.certificateResult != CovPassCheckValidationResult.Success
            )

    private fun textForActivationButton() =
        when {
            args.testCertificateData == null && args.certificateData != null &&
                args.certificateData?.certificateResult == CovPassCheckValidationResult.Success -> {
                R.string.result_2G_button_scan_test
            }
            args.testCertificateData != null && args.certificateData == null &&
                args.testCertificateData?.certificateResult == CovPassCheckValidationResult.Success -> {
                R.string.result_2G_button_scan_gproof
            }
            args.testCertificateData == null && args.certificateData != null &&
                args.certificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                R.string.result_2G_button_startover
            }
            args.testCertificateData != null && args.certificateData == null &&
                args.testCertificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                R.string.result_2G_button_startover
            }
            args.testCertificateData != null && args.certificateData != null &&
                args.certificateData?.certificateResult == CovPassCheckValidationResult.Success &&
                args.testCertificateData?.certificateResult == CovPassCheckValidationResult.Success -> {
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
            args.certificateData != null &&
                args.certificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(null, args.testCertificateData)
            }
            args.testCertificateData != null &&
                args.testCertificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(args.certificateData, null)
            }
            else -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(args.certificateData, args.testCertificateData)
            }
        }
    }

    override fun onValidationResultClosed() {}

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onBackPressed(): Abortable {
        when {
            args.certificateData != null &&
                args.certificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(null, args.testCertificateData)
            }
            args.testCertificateData != null &&
                args.testCertificateData?.certificateResult != CovPassCheckValidationResult.Success -> {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidatingFirstCertificate(args.certificateData, null)
            }
            else -> {
                findNavigator().popAll()
            }
        }
        return Abort
    }
}

@Parcelize
public data class ValidationResult2gData(
    public val certificateName: String?,
    public val certificateTransliteratedName: String?,
    public val certificateBirthDate: String?,
    public val sampleCollection: ZonedDateTime?,
    public val certificateResult: CovPassCheckValidationResult,
    public val certificateId: String?,
    public val type: ValidationResult2gCertificateType,
    public val validFrom: Instant? = null,
) : Parcelable {
    public fun isBooster(): Boolean =
        type == ValidationResult2gCertificateType.Booster

    public fun isVaccination(): Boolean =
        type == ValidationResult2gCertificateType.Vaccination

    public fun isTestPCR(): Boolean =
        type == ValidationResult2gCertificateType.PCRTest
}

@Parcelize
public enum class ValidationResult2gCertificateType : Parcelable {
    Booster,
    Vaccination,
    Recovery,
    PCRTest,
    AntigenTest,
    NullCertificateOrUnknown
}
