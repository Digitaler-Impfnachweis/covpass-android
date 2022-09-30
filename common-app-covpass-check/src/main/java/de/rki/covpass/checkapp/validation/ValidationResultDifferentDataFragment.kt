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
import de.rki.covpass.checkapp.databinding.ValidationResultDifferentDataBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationResultDifferentDataFragmentNav(
    public val name1: String,
    public val transliteratedName1: String,
    public val birthDate1: String,
    public val name2: String,
    public val transliteratedName2: String,
    public val birthDate2: String,
    public val dateDifferent: Boolean,
    public val expertModeData: ExpertModeData?,
    public val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultDifferentDataFragment::class)

public class ValidationResultDifferentDataFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<ValidationResultDifferentDataFragmentNav>() }
    private val binding by viewBinding(ValidationResultDifferentDataBinding::inflate)

    override val announcementAccessibilityRes: Int = R.string.accessibility_warning_2G_names_announce_open

    override val buttonTextRes: Int = R.string.result_2G_button_retry

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.warning_2G_names_title)
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationResultClosed()
            }
            setText(R.string.result_2G_button_startover)
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

        binding.validationResultDifferentDataSecondCertificateDataElement.showInfo(
            R.drawable.validation_result_2g_data_warning,
            args.name2,
            args.transliteratedName2,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.birthDate2,
            ),
            true,
        )

        binding.validationResultDifferentDataValidDifferenceButton.setOnClickListener {
            findNavigator().push(
                ValidationResultSuccessFragmentNav(
                    args.name1,
                    args.transliteratedName1,
                    args.birthDate1,
                    args.expertModeData,
                    args.isGermanCertificate,
                ),
            )
        }

        binding.validationResultDifferentDataValidDifferenceTitle.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceText.isVisible = !args.dateDifferent
        binding.validationResultDifferentDataValidDifferenceButton.isVisible = !args.dateDifferent

        startTimer()
    }

    private fun tryAgainAndBackEvent() {
        findNavigator().popUntil<ValidationResultListener>()?.onValidationFirstScanFinish()
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
