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
import de.rki.covpass.sdk.utils.getDisplayString
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.hoursTillNow
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

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

    abstract val title: String
    abstract val text: String
    abstract val imageRes: Int

    open val titleInfo1: String? = null
    open val textInfo1: String? = null
    open val imageInfo1Res: Int = 0

    open val titleInfo2: String? = null
    open val textInfo2: String? = null
    open val imageInfo2Res: Int = 0

    open val titleInfo3: String? = null
    open val textInfo3: String? = null
    open val imageInfo3Res: Int = 0

    open val titleInfo4: String? = null
    open val textInfo4: String? = null
    open val imageInfo4Res: Int = 0

    open val textFooter: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultImage.setImageResource(imageRes)
        binding.resultTitle.text = title
        binding.resultText.text = text

        binding.resultInfoLayout1.isVisible = textInfo1 != null
        binding.resultInfoImage1.isVisible = imageInfo1Res != 0
        textInfo1?.let {
            binding.resultInfoImage1.setImageResource(imageInfo1Res)
            binding.resultInfoTitle1.text = titleInfo1
            binding.resultInfoText1.text = it
        }

        binding.resultInfoLayout2.isVisible = textInfo2 != null
        binding.resultInfoImage2.isVisible = textInfo2 != null
        if (textInfo2 != null) {
            binding.resultInfoImage2.setImageResource(imageInfo2Res)
            binding.resultInfoTitle2.text = titleInfo2
            binding.resultInfoText2.text = textInfo2
        }

        binding.resultInfoLayout3.isVisible = textInfo3 != null
        binding.resultInfoImage3.isVisible = textInfo3 != null
        if (textInfo3 != null) {
            binding.resultInfoImage3.setImageResource(imageInfo3Res)
            binding.resultInfoTitle3.text = titleInfo3
            binding.resultInfoText3.text = textInfo3
        }

        binding.resultInfoLayout4.isVisible = textInfo4 != null
        binding.resultInfoImage4.isVisible = textInfo4 != null
        if (textInfo4 != null) {
            binding.resultInfoImage4.setImageResource(imageInfo4Res)
            binding.resultInfoTitle4.text = titleInfo4
            binding.resultInfoText4.text = textInfo4
        }

        binding.resultInfoFooter.isVisible = textFooter != null
        if (textFooter != null) {
            binding.resultInfoFooter.text = textFooter
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
internal class ValidationResultSuccessNav(
    val name: String,
    val birthDate: String
) : FragmentNav(ValidationResultSuccessFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationResultSuccessNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_message)
    }
    override val imageRes = R.drawable.result_success_image

    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_date_of_birth, args.birthDate)
    }
    override val imageInfo1Res = R.drawable.result_person

    override val textFooter by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_note)
    }

    override val buttonTextRes = R.string.validation_check_popup_valid_vaccination_button_title
}

@Parcelize
internal class ValidPcrTestFragmentNav(
    val name: String,
    val birthDate: String,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(ValidPcrTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the PCR test is valid.
 */
internal class ValidPcrTestResultFragment : ValidationResultFragment() {
    private val args: ValidPcrTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_less_than_72_h_title,
            args.sampleCollection?.hoursTillNow() ?: 0
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_pcr_test_less_than_72_h_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_less_than_72_h_date_of_birth,
            args.birthDate
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_less_than_72_h_date_of_issue,
            args.sampleCollection?.toLocalDateTime()?.formatDateTime()
        )
    }
    override val textInfo2 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_less_than_72_h_utc,
            args.sampleCollection?.offset?.getDisplayString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_valid_pcr_test_less_than_72_h_button_title
}

@Parcelize
internal class ValidAntigenTestFragmentNav(
    val name: String,
    val birthDate: String,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(ValidAntigenTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Antigen test is valid.
 */
internal class ValidAntigenTestResultFragment : ValidationResultFragment() {
    private val args: ValidAntigenTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_test_less_than_24_h_title,
            args.sampleCollection?.hoursTillNow() ?: 0
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_test_less_than_24_h_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_test_less_than_24_h_date_of_birth,
            args.birthDate
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(
            R.string.validation_check_popup_test_less_than_24_h_date_of_issue,
            args.sampleCollection?.toLocalDateTime()?.formatDateTime()
        )
    }
    override val textInfo2 by lazy {
        getString(
            R.string.validation_check_popup_test_less_than_24_h_utc,
            args.sampleCollection?.offset?.getDisplayString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_test_less_than_24_h_button_title
}

@Parcelize
internal class ValidationResultFailureFragmentNav : FragmentNav(ValidationResultFailureFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation failure.
 */
internal class ValidationResultFailureFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_message)
    }

    override val imageInfo1Res = R.drawable.result_cert_recovery
    override val titleInfo1 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate__recovery_title)
    }
    override val textInfo1 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_recovery_body)
    }

    override val imageInfo2Res = R.drawable.result_cert_vaccination
    override val titleInfo2 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate__vaccination_title)
    }
    override val textInfo2 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_vaccination_body)
    }

    override val imageInfo3Res = R.drawable.result_cert_test
    override val titleInfo3 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate__test_title)
    }
    override val textInfo3 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_test_body)
    }

    override val imageInfo4Res = R.drawable.result_invalid
    override val titleInfo4 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate__problem_title)
    }
    override val textInfo4 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_certificate_problem_body)
    }

    override val buttonTextRes = R.string.validation_check_popup_unsuccessful_certificate_button_title
}
