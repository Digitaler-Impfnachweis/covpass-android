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
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.hoursTillNow
import de.rki.covpass.sdk.utils.toDeviceTimeZone
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

    override val heightLayoutParams: Int by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_result_announce

    private val binding by viewBinding(ValidationResultBinding::inflate)

    abstract val title: String
    abstract val text: String
    abstract val imageRes: Int

    open val titleInfo1: String? = null
    open val subtitleInfo1: String? = null
    open val textInfo1: String? = null
    open val imageInfo1Res: Int = 0

    open val titleInfo2: String? = null
    open val textInfo2: String? = null
    open val imageInfo2Res: Int = 0

    open val titleInfo3: String? = null
    open val textInfo3: String? = null
    open val imageInfo3Res: Int = 0

    open val textFooter: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultImage.setImageResource(imageRes)
        binding.resultTitle.text = title
        binding.resultText.text = text

        binding.resultInfoLayout1.isVisible = textInfo1 != null
        binding.resultInfoImage1.isVisible = imageInfo1Res != 0
        binding.resultInfoSubtitle1.isVisible = subtitleInfo1 != null
        textInfo1?.let {
            binding.resultInfoImage1.setImageResource(imageInfo1Res)
            binding.resultInfoTitle1.text = titleInfo1
            binding.resultInfoSubtitle1.text = subtitleInfo1
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
    val transliteratedName: String,
    val birthDate: String,
    val vaccinationNumberOfMonthsAgo: Int?,
    val isBooster : Boolean,
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
    override val subtitleInfo1 by lazy { args.transliteratedName }
    override val textInfo1 by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_date_of_birth, args.birthDate) +
        if (args.vaccinationNumberOfMonthsAgo != null) {
            "\n" +
                getString(R.string.validation_check_popup_valid_vaccination_date, args.vaccinationNumberOfMonthsAgo)
        } else ""
    }
    override val imageInfo1Res = R.drawable.result_person

    override val textFooter by lazy {
        if (args.isBooster) {
            getString(R.string.validation_check_popup_valid_vaccination_booster)
        } else {
            getString(R.string.validation_check_popup_valid_vaccination_recovery_note)
        }
    }

    override val buttonTextRes = R.string.validation_check_popup_valid_vaccination_button_title
}

@Parcelize
internal class ValidPcrTestFragmentNav(
    val name: String,
    val transliteratedName: String,
    val birthDate: String,
    val sampleCollection: ZonedDateTime?,
) : FragmentNav(ValidPcrTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the PCR test is valid.
 */
internal class ValidPcrTestResultFragment : ValidationResultFragment() {
    private val args: ValidPcrTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_title,
            args.sampleCollection?.hoursTillNow() ?: 0
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_pcr_test_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val subtitleInfo1 by lazy { args.transliteratedName }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_valid_pcr_test_date_of_birth,
            args.birthDate
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        args.sampleCollection?.toDeviceTimeZone()?.toLocalDateTime()?.formatDateTime()
    }
    override val textInfo2 by lazy {
        getString(R.string.validation_check_popup_valid_pcr_test_date_of_issue)
    }
    override val buttonTextRes = R.string.validation_check_popup_valid_pcr_test_button_title
}

@Parcelize
internal class ValidAntigenTestFragmentNav(
    val name: String,
    val transliteratedName: String,
    val birthDate: String,
    val sampleCollection: ZonedDateTime?,
) : FragmentNav(ValidAntigenTestResultFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display that the Antigen test is valid.
 */
internal class ValidAntigenTestResultFragment : ValidationResultFragment() {
    private val args: ValidAntigenTestFragmentNav by lazy { getArgs() }
    override val imageRes = R.drawable.result_test_image
    override val title by lazy {
        getString(
            R.string.validation_check_popup_test_title,
            args.sampleCollection?.hoursTillNow() ?: 0
        )
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_test_message)
    }
    override val imageInfo1Res = R.drawable.result_person
    override val titleInfo1 by lazy { args.name }
    override val subtitleInfo1 by lazy { args.transliteratedName }
    override val textInfo1 by lazy {
        getString(
            R.string.validation_check_popup_test_date_of_birth,
            args.birthDate
        )
    }
    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        args.sampleCollection?.toDeviceTimeZone()?.toLocalDateTime()?.formatDateTime()
    }
    override val textInfo2 by lazy {
        getString(R.string.validation_check_popup_test_date_of_issue)
    }
    override val buttonTextRes = R.string.validation_check_popup_test_button_title
}

@Parcelize
internal class ValidationResultFailureFragmentNav : FragmentNav(ValidationResultFailureFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation failure.
 */
internal class ValidationResultFailureFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_title)
    }
    override val text by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subline)
    }

    override val imageInfo1Res = R.drawable.result_invalid_expired_test
    override val titleInfo1 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_expiration)
    }
    override val textInfo1 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_expiration_text)
    }

    override val imageInfo2Res = R.drawable.result_calendar
    override val titleInfo2 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_protection)
    }
    override val textInfo2 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_protection_text)
    }

    override val imageInfo3Res = R.drawable.result_cert_vaccination
    override val titleInfo3 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted)
    }
    override val textInfo3 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted_text)
    }

    override val buttonTextRes = R.string.technical_validation_check_popup_valid_vaccination_button_further
}

@Parcelize
internal class ValidationResultTechnicalFailureFragmentNav :
    FragmentNav(ValidationResultTechnicalFailureFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation failure.
 */
internal class ValidationResultTechnicalFailureFragment : ValidationResultFragment() {
    override val imageRes = R.drawable.result_failure_image
    override val title by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_title)
    }
    override val text by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_subline)
    }

    override val imageInfo1Res = R.drawable.result_invalid_technical_signature
    override val titleInfo1 by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_signature_subheading)
    }
    override val textInfo1 by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_signature_subline)
    }

    override val imageInfo2Res = R.drawable.result_invalid_technical_qr
    override val titleInfo2 by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_qrreadibility_subheading)
    }
    override val textInfo2 by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_qrreadibility_subline)
    }

    override val buttonTextRes = R.string.technical_validation_check_popup_valid_vaccination_button_title
}
