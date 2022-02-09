/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ValidationResult2gPlusBBoosterBinding
import de.rki.covpass.checkapp.scanner.ValidationResult2gData
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.utils.daysTillNow
import kotlinx.parcelize.Parcelize

@Parcelize
public class ValidationResult2gPlusBBoosterFragmentNav(
    public val certificateData: ValidationResult2gData?,
) : FragmentNav(ValidationResult2gPlusBBoosterFragment::class)

public class ValidationResult2gPlusBBoosterFragment : BaseBottomSheet() {

    private val args by lazy { getArgs<ValidationResult2gPlusBBoosterFragmentNav>() }
    private val binding by viewBinding(ValidationResult2gPlusBBoosterBinding::inflate)

    override val buttonTextRes: Int = R.string.result_2G_button_startover
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

        fillDataElement()
    }

    private fun fillCertificateElement() {
        binding.validationResult2gPlusBCertificate.showValidCertificate(
            R.drawable.validation_result_2g_valid_certificate,
            getString(R.string.result_2G_2nd_booster_valid),
            getString(
                R.string.result_2G_2nd_timestamp_days,
                args.certificateData?.validFrom?.daysTillNow()
            )
        )
    }

    private fun fillDataElement() {
        binding.validationResult2gPlusBInfoText.setText(R.string.validation_check_popup_valid_pcr_test_message)
        binding.validationResult2gPlusBInfoElement.showInfo(
            R.drawable.validation_result_2g_data,
            args.certificateData?.certificateName,
            args.certificateData?.certificateTransliteratedName,
            getString(
                R.string.validation_check_popup_valid_vaccination_date_of_birth,
                args.certificateData?.certificateBirthDate,
            )
        )
    }

    override fun onActionButtonClicked() {
        findNavigator().popUntil<ValidationResult2GListener>()
            ?.onValidationResetOrFinish()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onBackPressed(): Abortable {
        onCloseButtonClicked()
        return Abort
    }
}
