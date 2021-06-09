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
import de.rki.covpass.sdk.utils.adjustToString
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.hoursTillNow
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
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
internal class FullVaccinationFragmentNav(
    val name: String,
    val birthDate: LocalDate?
) : FragmentNav(FullVaccinationResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class FullVaccinationResultFragment : ValidationResultFragment() {
    private val args: FullVaccinationFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_message)
    }
    override val imageRes = R.drawable.result_success_image
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(R.string.validation_check_popup_date_of_birth_at_pattern, args.birthDate.formatDateOrEmpty())
    }
    override val imageInfo1Res = R.drawable.result_person
    override val buttonTextRes = R.string.validation_check_popup_valid_vaccination_button_title
}

@Parcelize
internal class PartialVaccinationFragmentNav : FragmentNav(PartialVaccinationResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the protection is not complete yet.
 */
internal class PartialVaccinationResultFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_incomplete_image
    override val title by lazy {
        getString(R.string.validation_check_popup_vaccination_not_completely_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_vaccination_not_completely_message)
    }
    override val buttonTextRes = R.string.validation_check_popup_partial_valid_vaccination_button_title
}

@Parcelize
internal class NegativeValidPcrTestFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(NegativeValidPcrTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Negative PCR test is within 72h.
 */
internal class NegativeValidPcrTestResultFragment : ValidationResultFragment() {
    private val args: NegativeValidPcrTestFragmentNav by lazy { getArgs() }
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
            args.birthDate.formatDateOrEmpty()
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
            args.sampleCollection?.offset?.adjustToString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_valid_pcr_test_less_than_72_h_button_title
}

@Parcelize
internal class NegativeExpiredPcrTestFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(NegativeExpiredPcrTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Negative PCR test after 72h.
 */
internal class NegativeExpiredPcrTestResultFragment : ValidationResultFragment() {
    private val args: NegativeExpiredPcrTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_older_than_72_h_title,
            args.sampleCollection?.toLocalDate().formatDateOrEmpty()
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_pcr_test_older_than_72_h_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_older_than_72_h_date_of_birth,
            args.birthDate.formatDateOrEmpty()
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_older_than_72_h_date_of_issue,
            args.sampleCollection?.toLocalDateTime()?.formatDateTime()
        )
    }
    override val textInfo2 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_older_than_72_h_utc,
            args.sampleCollection?.offset?.adjustToString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_valid_pcr_test_older_than_72_h_button_title
}

@Parcelize
internal class PositivePcrTestFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(PositivePcrTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display the Positive PCR test.
 */
internal class PositivePcrTestResultFragment : ValidationResultFragment() {
    private val args: PositivePcrTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_pcr_test_positive_title,
            args.sampleCollection?.toLocalDate().formatDateOrEmpty()
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_pcr_test_positive_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_pcr_test_positive_date_of_birth,
            args.birthDate.formatDateOrEmpty()
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(
            R.string.validation_check_popup_pcr_test_positive_date_of_issue,
            args.sampleCollection?.toLocalDateTime()?.formatDateTime()
        )
    }
    override val textInfo2 by lazy {
        getString(
            R.string.validation_check_popup_pcr_test_positive_utc,
            args.sampleCollection?.offset?.adjustToString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_pcr_test_positive_button_title
}

@Parcelize
internal class NegativeValidAntigenTestFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(NegativeValidAntigenTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Negative Antigen test is within 48h.
 */
internal class NegativeValidAntigenTestResultFragment : ValidationResultFragment() {
    private val args: NegativeValidAntigenTestFragmentNav by lazy { getArgs() }
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
            args.birthDate.formatDateOrEmpty()
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
            args.sampleCollection?.offset?.adjustToString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_test_less_than_24_h_button_title
}

@Parcelize
internal class NegativeExpiredAntigenTestFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
    val sampleCollection: ZonedDateTime?
) : FragmentNav(NegativeExpiredAntigenTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Negative Antigen test is after 48h.
 */
internal class NegativeExpiredAntigenTestResultFragment : ValidationResultFragment() {
    private val args: NegativeExpiredAntigenTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_test_older_than_24_h_title,
            args.sampleCollection?.toLocalDate().formatDateOrEmpty()
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_test_older_than_24_h_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_test_older_than_24_h_date_of_birth,
            args.birthDate.formatDateOrEmpty()
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(
            R.string.validation_check_popup_test_older_than_24_h_date_of_issue,
            args.sampleCollection?.toLocalDateTime()?.formatDateTime()
        )
    }
    override val textInfo2 by lazy {
        getString(
            R.string.validation_check_popup_test_older_than_24_h_utc,
            args.sampleCollection?.offset?.adjustToString()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_test_older_than_24_h_button_title
}

@Parcelize
internal class PositiveAntigenTestFragmentNav(
    val sampleCollection: ZonedDateTime?
) : FragmentNav(PositiveAntigenTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display the Positive Antigen test.
 */
internal class PositiveAntigenTestResultFragment : ValidationResultFragment() {
    private val args: PositiveAntigenTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_test_positive_title,
            args.sampleCollection?.toLocalDate().formatDateOrEmpty()
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_test_positive_message)
    }
    override val buttonTextRes = R.string.validation_check_popup_test_positive_button_title
}

@Parcelize
internal class ValidRecoveryCertFragmentNav(
    val name: String,
    val birthDate: LocalDate?
) : FragmentNav(ValidRecoveryCertResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Recovery Certificate is within 180 days.
 */
internal class ValidRecoveryCertResultFragment : ValidationResultFragment() {
    private val args: ValidRecoveryCertFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_success_image
    override val title by lazy {
        getString(R.string.validation_check_popup_recovery_proven_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_recovery_proven_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_recovery_proven_date_of_birth,
            args.birthDate.formatDateOrEmpty()
        )
    }
    override val buttonTextRes = R.string.validation_check_popup_recovery_proven_button_title
}

@Parcelize
internal class ExpiredRecoveryCertFragmentNav : FragmentNav(ExpiredRecoveryCertResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Recovery Certificate is after 180 days.
 */
internal class ExpiredRecoveryCertResultFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(R.string.validation_check_popup_recovery_expired_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_recovery_expired_message)
    }
    override val buttonTextRes = R.string.validation_check_popup_recovery_expired_button_title
}

@Parcelize
internal class ValidationResultFailureFragmentNav : FragmentNav(ValidationResultFailureFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation failure.
 */
internal class ValidationResultFailureFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_title)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_message)
    }
    override val imageInfo1Res = R.drawable.result_search
    override val titleInfo1 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_first_reason_title)
    }
    override val textInfo1 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_first_reason_body)
    }
    override val imageInfo2Res = R.drawable.result_invalid
    override val titleInfo2 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_second_reason_title)
    }
    override val textInfo2 by lazy {
        getString(R.string.validation_check_popup_unsuccessful_test_second_reason_body)
    }
    override val buttonTextRes = R.string.validation_check_popup_unsuccessful_test_button_title
}
