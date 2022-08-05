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
import de.rki.covpass.checkapp.databinding.ValidationResult2gDifferentDataBinding
import de.rki.covpass.checkapp.scanner.ValidationResult2gData
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationResult2gDifferentDataFragmentNav(
    public val firstCertificateData: ValidationResult2gData,
    public val secondCertificateData: ValidationResult2gData,
    public val dateDifferent: Boolean,
) : FragmentNav(ValidationResult2gDifferentDataFragment::class)

public class ValidationResult2gDifferentDataFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<ValidationResult2gDifferentDataFragmentNav>() }
    private val binding by viewBinding(ValidationResult2gDifferentDataBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_warning_2G_names_announce_open

    // TODO add close announcement accessibility_warning_2G_names_announce_close
    override val buttonTextRes: Int = R.string.result_2G_button_retry

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.warning_2G_names_title)
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidationResetOrFinish()
            }
            setText(R.string.result_2G_button_startover)
        }

        binding.validationResultDifferentDataCertificateDataElement.showInfo(
            R.drawable.validation_result_2g_data,
            args.firstCertificateData.certificateName,
            args.firstCertificateData.certificateTransliteratedName,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.firstCertificateData.certificateBirthDate,
            ),
        )

        binding.validationResultDifferentDataTestDataElement.showInfo(
            R.drawable.validation_result_2g_data_warning,
            args.secondCertificateData.certificateName,
            args.secondCertificateData.certificateTransliteratedName,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.secondCertificateData.certificateBirthDate,
            ),
            true,
        )

        binding.validationResultDifferentDataValidDifferenceButton.setOnClickListener {
            findNavigator().push(
                ValidationResult2gFragmentNav(
                    args.firstCertificateData,
                    args.secondCertificateData,
                ),
            )
        }

        binding.validationResultDifferentDataValidDifferenceTitle.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceText.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceButton.isVisible = !args.dateDifferent

        startTimer()
    }

    private fun tryAgainAndBackEvent() {
        findNavigator().popUntil<ValidationResult2GListener>()
            ?.onValidatingFirstCertificate(args.firstCertificateData)
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
