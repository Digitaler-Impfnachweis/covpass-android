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
import de.rki.covpass.commonapp.BaseBottomSheet
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationResult2gDifferentDataFragmentNav(
    public val certificateData: ValidationResult2gData,
    public val testCertificateData: ValidationResult2gData,
    public val certificateFirst: Boolean,
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
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResult2GListener>()
                    ?.onValidationResetOrFinish()
            }
            setText(R.string.result_2G_button_startover)
        }

        binding.validationResultDifferentDataCertificateDataElement.showInfo(
            if (args.certificateFirst) R.drawable.validation_result_2g_data
            else R.drawable.validation_result_2g_data_warning,
            args.certificateData.certificateName,
            args.certificateData.certificateTransliteratedName,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.certificateData.certificateBirthDate
            ),
            warning = !args.certificateFirst
        )

        binding.validationResultDifferentDataTestDataElement.showInfo(
            if (args.certificateFirst) R.drawable.validation_result_2g_data_warning
            else R.drawable.validation_result_2g_data,
            args.testCertificateData.certificateName,
            args.testCertificateData.certificateTransliteratedName,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.testCertificateData.certificateBirthDate,
            ),
            args.certificateFirst
        )

        binding.validationResultDifferentDataValidDifferenceButton.setOnClickListener {
            findNavigator().push(
                ValidationResult2gFragmentNav(
                    args.certificateData,
                    args.testCertificateData
                )
            )
        }

        binding.validationResultDifferentDataValidDifferenceTitle.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceText.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceButton.isVisible = !args.dateDifferent
    }

    private fun tryAgainAndBackEvent() {
        if (args.certificateFirst) {
            findNavigator().popUntil<ValidationResult2GListener>()
                ?.onValidatingFirstCertificate(args.certificateData, null)
        } else {
            findNavigator().popUntil<ValidationResult2GListener>()
                ?.onValidatingFirstCertificate(null, args.testCertificateData)
        }
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
