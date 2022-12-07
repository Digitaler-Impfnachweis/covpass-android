/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validation

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ValidationResultDifferentDataImmunityCheckBinding
import de.rki.covpass.checkapp.scanner.DataComparison3Certs
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationResultDifferentDataImmunityCheckFragmentNav(
    public val name1: String,
    public val transliteratedName1: String,
    public val birthDate1: String,
    public val name2: String,
    public val transliteratedName2: String,
    public val birthDate2: String,
    public val name3: String?,
    public val transliteratedName3: String?,
    public val birthDate3: String?,
    public val dataComparison3Certs: DataComparison3Certs,
    public val isPendingStatus: Boolean = false,
    public val numberOfCertificates: Int,
    public val expertModeData: ExpertModeData?,
    public val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultDifferentDataImmunityCheckFragment::class)

public class ValidationResultDifferentDataImmunityCheckFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<ValidationResultDifferentDataImmunityCheckFragmentNav>() }
    private val binding by viewBinding(ValidationResultDifferentDataImmunityCheckBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_warning_2G_names_announce_open

    override val buttonTextRes: Int = R.string.technical_validation_check_popup_retry

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.warning_2G_names_title)
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationResultClosed()
            }
            setText(R.string.technical_validation_check_popup_valid_vaccination_button_3_title)
        }

        binding.validationResultDifferentDataFirstCertificateDataElement.showInfo(
            R.drawable.validation_result_2g_data,
            args.name1,
            args.transliteratedName1,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.birthDate1,
            ),
        )

        val isSecondCertDiff =
            args.dataComparison3Certs == DataComparison3Certs.SecondDifferentName ||
                args.dataComparison3Certs == DataComparison3Certs.SecondDifferentDate ||
                args.dataComparison3Certs == DataComparison3Certs.AllDifferentName ||
                args.dataComparison3Certs == DataComparison3Certs.AllDifferentDate

        binding.validationResultDifferentDataSecondCertificateDataElement.showInfo(
            if (isSecondCertDiff) {
                R.drawable.validation_result_2g_data_warning
            } else {
                R.drawable.validation_result_2g_data
            },
            args.name2,
            args.transliteratedName2,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.birthDate2,
            ),
            isSecondCertDiff,
        )

        if (args.numberOfCertificates == 3) {
            binding.validationResultDifferentDataThirdCertificateDataElement.isVisible = true
            binding.validationResultDifferentDataThirdCertificateTitle.isVisible = true
            val isThirdCertDiff =
                args.dataComparison3Certs == DataComparison3Certs.ThirdDifferentName ||
                    args.dataComparison3Certs == DataComparison3Certs.ThirdDifferentDate ||
                    args.dataComparison3Certs == DataComparison3Certs.AllDifferentName ||
                    args.dataComparison3Certs == DataComparison3Certs.AllDifferentDate
            binding.validationResultDifferentDataThirdCertificateDataElement.showInfo(
                if (isThirdCertDiff) {
                    R.drawable.validation_result_2g_data_warning
                } else {
                    R.drawable.validation_result_2g_data
                },
                args.name3,
                args.transliteratedName3,
                getString(
                    R.string.validation_check_popup_valid_vaccination_date_of_birth,
                    args.birthDate3,
                ),
                isThirdCertDiff,
            )
        } else {
            binding.validationResultDifferentDataThirdCertificateDataElement.isVisible = false
            binding.validationResultDifferentDataThirdCertificateTitle.isVisible = false
        }

        binding.validationResultDifferentDataValidDifferenceButton.setOnClickListener {
            if (args.isPendingStatus) {
                if (args.numberOfCertificates < 3) {
                    findNavigator().push(
                        ValidationPendingResultFragmentNav(
                            args.expertModeData,
                            args.isGermanCertificate,
                            args.numberOfCertificates,
                        ),
                    )
                } else {
                    findNavigator().push(
                        ValidationImmunityResultIncompleteFragmentNav(
                            args.expertModeData,
                            args.isGermanCertificate,
                        ),
                    )
                }
            } else {
                findNavigator().push(
                    ValidationImmunityResultSuccessFragmentNav(
                        name = args.name1,
                        transliteratedName = args.transliteratedName1,
                        birthDate = args.birthDate1,
                        expertModeData = args.expertModeData,
                        isGermanCertificate = args.isGermanCertificate,
                    ),
                )
            }
        }

        val isDateDifferent =
            args.dataComparison3Certs == DataComparison3Certs.SecondDifferentDate ||
                args.dataComparison3Certs == DataComparison3Certs.ThirdDifferentDate ||
                args.dataComparison3Certs == DataComparison3Certs.AllDifferentDate
        binding.validationResultDifferentDataValidDifferenceTitle.isVisible = !isDateDifferent
        binding.validationResultDifferentDataValidDifferenceText.isVisible = !isDateDifferent
        binding.validationResultDifferentDataValidDifferenceButton.isVisible = !isDateDifferent

        startTimer()
    }

    private fun tryAgainAndBackEvent() {
        findNavigator().popUntil<ValidationResultListener>()?.onValidationRetryLastScan()
    }

    override fun onActionButtonClicked() {
        tryAgainAndBackEvent()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onBackPressed(): Abortable {
        tryAgainAndBackEvent()
        return Abort
    }
}
