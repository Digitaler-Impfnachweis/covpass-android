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
import de.rki.covpass.checkapp.databinding.ValidationPendingResultBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationPendingResultFragmentNav(
    public val expertModeData: ExpertModeData?,
    public val germanCertificate: Boolean,
    public val numberOfCertificates: Int,
) : FragmentNav(ValidationPendingResultFragment::class)

public class ValidationPendingResultFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<ValidationPendingResultFragmentNav>() }
    private val binding by viewBinding(ValidationPendingResultBinding::inflate)

    // TODO change string
    override val announcementAccessibilityRes: Int? = null

    override val buttonTextRes: Int = R.string.validation_check_popup_unsuccessful_certificate_button_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(
            R.string.functional_validation_check_popup_unsuccessful_certificate_title,
        )
        bottomSheetBinding.bottomSheetSubtitle.setText(
            R.string.functional_validation_check_popup_unsuccessful_certificate_subtitle,
        )
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationResultClosed()
            }
            setText(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted_button)
        }

        binding.validationResultFirstCertificate.showCertificate(
            R.drawable.validation_pending_result_certificate_icon,
            getString(R.string.functional_validation_check_popup_second_scan_blue_card_1_title),
            getString(R.string.functional_validation_check_popup_second_scan_blue_card_1_subtitle),
        )
        binding.validationResultSecondCertificate.isVisible = args.numberOfCertificates > 1
        if (args.numberOfCertificates > 1) {
            binding.validationResultSecondCertificate.showCertificate(
                R.drawable.validation_pending_result_certificate_icon,
                getString(R.string.functional_validation_check_popup_second_scan_blue_card_2_title),
                getString(R.string.functional_validation_check_popup_second_scan_blue_card_1_subtitle),
            )
        }
        binding.validationResultThirdCertificate.showCertificate(
            R.drawable.validation_pending_result_empty_icon,
            if (args.numberOfCertificates == 1) {
                getString(R.string.functional_validation_check_popup_second_scan_blue_card_2_title)
            } else {
                getString(R.string.functional_validation_check_popup_second_scan_blue_card_3_title)
            },
            getString(R.string.functional_validation_check_popup_second_scan_blue_card_2_subtitle),
        )
        binding.validationResultInfoElement.showWarning(
            title = getString(R.string.functional_validation_check_popup_second_scan_hint_title),
            subtitle = getString(R.string.functional_validation_check_popup_second_scan_hint_copy),
            subtitleStyle = R.style.DefaultText_OnBackground,
            iconRes = R.drawable.info_warning,
            subtitleTopMarginDimenRes = R.dimen.grid_one,
        )

        startTimer()
    }

    override fun onActionButtonClicked() {
        continueToNextScan()
    }

    private fun continueToNextScan() {
        findNavigator().popUntil<ValidationResultListener>()?.onValidationContinueToNextScan()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onBackPressed(): Abortable {
        continueToNextScan()
        return Abort
    }
}
