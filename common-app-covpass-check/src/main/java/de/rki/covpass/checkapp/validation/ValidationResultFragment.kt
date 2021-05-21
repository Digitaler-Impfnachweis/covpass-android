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
import com.ibm.health.common.navigation.android.triggerBackPress
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ValidationResultBinding
import de.rki.covpass.checkapp.main.MainFragment
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.android.utils.formatDateOrEmpty
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Interface to get notified when the validation result fragment was closed.
 */
internal interface ValidationResultListener {
    fun onValidationResultClosed()
}

/**
 * Common base class for displaying validation success, incomplete or failure result.
 */
internal abstract class ValidationResultFragment : BaseBottomSheet() {

    override val heightLayoutParams = ViewGroup.LayoutParams.MATCH_PARENT

    private val binding by viewBinding(ValidationResultBinding::inflate)

    abstract val titleRes: Int
    abstract val textRes: Int
    abstract val imageRes: Int

    open val titleInfo1String: String? = null
    open val textInfo1String: String? = null
    abstract val imageInfo1Res: Int

    open val titleInfo2Res: Int = 0
    open val textInfo2Res: Int = 0
    open val imageInfo2Res: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultTitle.text = getString(titleRes)
        binding.resultText.text = getString(textRes)
        binding.resultImage.setImageResource(imageRes)

        binding.resultInfoTitle1.text = titleInfo1String
        binding.resultInfoText1.text = textInfo1String
        binding.resultInfoImage1.setImageResource(imageInfo1Res)

        binding.resultInfoLayout2.isVisible = textInfo2Res != 0
        binding.resultInfoImage2.isVisible = textInfo2Res != 0
        if (textInfo2Res != 0) {
            binding.resultInfoTitle2.text = getString(titleInfo2Res)
            binding.resultInfoText2.text = getString(textInfo2Res)
            binding.resultInfoImage2.setImageResource(imageInfo2Res)
        }
    }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<ValidationResultListener>()?.onValidationResultClosed()
        return Abort
    }

    override fun onActionButtonClicked() {
        triggerBackPress()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popUntil<MainFragment>()
    }
}

@Parcelize
internal class ValidationResultSuccessFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
) : FragmentNav(ValidationResultSuccessFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationResultSuccessFragmentNav by lazy { getArgs() }
    override val titleRes = R.string.validation_check_popup_valid_vaccination_title
    override val textRes = R.string.validation_check_popup_valid_vaccination_message
    override val imageRes = R.drawable.result_success_image
    override val titleInfo1String by lazy { args.name }
    override val textInfo1String by lazy {
        getString(R.string.validation_check_popup_date_of_birth_at_pattern, args.birthDate.formatDateOrEmpty())
    }
    override val imageInfo1Res = R.drawable.result_person
    override val buttonTextRes = R.string.validation_check_popup_valid_vaccination_button_title
}

@Parcelize
internal class ValidationResultIncompleteFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
) : FragmentNav(ValidationResultIncompleteFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the protection is not complete yet.
 */
internal class ValidationResultIncompleteFragment : ValidationResultFragment() {
    private val args: ValidationResultIncompleteFragmentNav by lazy { getArgs() }
    override val titleRes = R.string.validation_check_popup_vaccination_not_completely_title
    override val textRes = R.string.validation_check_popup_vaccination_not_completely_message
    override val imageRes = R.drawable.result_incomplete_image
    override val titleInfo1String by lazy { args.name }
    override val textInfo1String by lazy {
        getString(R.string.validation_check_popup_date_of_birth_at_pattern, args.birthDate.formatDateOrEmpty())
    }
    override val imageInfo1Res = R.drawable.result_person
    override val buttonTextRes = R.string.validation_check_popup_partial_valid_vaccination_button_title
}

@Parcelize
internal class ValidationResultFailureFragmentNav : FragmentNav(ValidationResultFailureFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation failure.
 */
internal class ValidationResultFailureFragment : ValidationResultFragment() {
    override val titleRes = R.string.validation_check_popup_unsuccessful_test_title
    override val textRes = R.string.validation_check_popup_unsuccessful_test_message
    override val imageRes = R.drawable.result_failure_image
    override val titleInfo1String by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_first_reason_title)
    }
    override val textInfo1String by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_first_reason_body)
    }
    override val imageInfo1Res = R.drawable.result_search
    override val titleInfo2Res = R.string.validation_check_popup_unsuccessful_test_second_reason_title
    override val textInfo2Res = R.string.validation_check_popup_unsuccessful_test_second_reason_body
    override val imageInfo2Res = R.drawable.hourglass
    override val buttonTextRes = R.string.validation_check_popup_unsuccessful_test_button_title
}
