/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.validation

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getSpanned
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
import de.rki.covpass.checkapp.revocation.RevocationExportFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.utils.SunsetChecker
import de.rki.covpass.commonapp.utils.isLandscapeMode
import de.rki.covpass.commonapp.utils.setExternalLinkImage
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

/**
 * Interface to get notified when the validation result fragment was closed.
 */
internal interface ValidationResultListener {
    fun onValidationFirstScanFinish()
    fun onValidationResultClosed()
    fun onValidationContinueToNextScan()
    fun onValidationRetryLastScan()
}

/**
 * Common base class for displaying validation success, incomplete or failure result.
 */
internal abstract class ValidationResultFragment : BaseBottomSheet() {

    override val heightLayoutParams: Int by lazy { ViewGroup.LayoutParams.MATCH_PARENT }
    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_result_announce
    override val closingAnnouncementAccessibilityRes: Int =
        R.string.accessibility_scan_result_closing_announce

    private val binding by viewBinding(ValidationResultBinding::inflate)

    abstract val title: String
    abstract val text: String
    abstract val imageRes: Int
    open val subtitle: String? = null

    open val name: String? = null
    open val transliteratedName: String? = null
    open val birthDate: String? = null

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

    open val textFooter: Int? = null

    private val expertModeVisible: Boolean by lazy {
        commonDeps.checkContextRepository.isExpertModeOn.value && expertModeData != null
    }

    open val regionText: String? = null

    open val allowSecondScan = false
    open val isGermanCertificate: Boolean = false
    open val expertModeData: ExpertModeData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLandscapeModeActionButtonPosition()
        if (allowSecondScan) {
            binding.resultLayoutSecondScan.isVisible = true
            binding.bottomSheetActionButton.setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationFirstScanFinish()
            }
        }
        binding.resultImage.setImageResource(imageRes)
        binding.resultTitle.text = title
        binding.resultText.text = text
        binding.resultRegionText.isVisible = regionText != null
        regionText?.let {
            binding.resultRegionText.text = regionText
        }
        subtitle?.let {
            binding.resultSubtitle.isVisible = true
            binding.resultSubtitle.text = subtitle
        }

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

        if (name != null) {
            binding.validationResultUserData.isVisible = true
            binding.validationResultUserData.showInfo(
                R.drawable.validation_result_2g_data,
                name,
                transliteratedName,
                getString(
                    R.string.validation_check_popup_valid_vaccination_date_of_birth,
                    birthDate,
                ),
            )
        }

        binding.resultInfoFooter.isGone = textFooter == null || SunsetChecker.isSunset()
        textFooter?.let {
            binding.resultInfoFooter.apply {
                text = getSpanned(it)
                movementMethod = LinkMovementMethod.getInstance()
                setExternalLinkImage()
            }
        }

        binding.revocationLegalNotification.isVisible = expertModeVisible
        binding.revocationLegalNotification.showInfo(
            title = getString(R.string.revocation_headline),
            subtitle = getString(R.string.validation_check_popup_revoked_certificate_box_text),
            iconRes = R.drawable.info_icon,
            description = getString(R.string.validation_check_popup_revoked_certificate_link_text),
            descriptionLink = {
                expertModeData?.let {
                    findNavigator().push(
                        RevocationExportFragmentNav(
                            revocationExportData = it,
                            isGermanCertificate = isGermanCertificate,
                        ),
                    )
                }
            },
            descriptionStyle = R.style.Header_Info_Small,
        )
        startTimer()
    }

    private fun setLandscapeModeActionButtonPosition() {
        val actionButtonLayoutParams =
            bottomSheetBinding.bottomSheetActionButton.layoutParams as? ConstraintLayout.LayoutParams
        actionButtonLayoutParams?.apply {
            horizontalBias = if (resources.isLandscapeMode()) 1f else 0.5f
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
    val transliteratedName: String,
    val birthDate: String,
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultSuccessFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationResultSuccessFragmentNav by lazy { getArgs() }

    override val title by lazy {
        getString(R.string.infschg_result_mask_optional_title)
    }
    override val text by lazy {
        getString(R.string.infschg_result_mask_optional_copy)
    }
    override val imageRes = R.drawable.status_mask_not_required

    override val name by lazy { args.name }
    override val transliteratedName by lazy { args.transliteratedName }
    override val birthDate by lazy { args.birthDate }

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationResultPartialFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
    val allowSecondCertificate: Boolean = false,
) : FragmentNav(ValidationResultPartialFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultPartialFragment : ValidationResultFragment() {
    private val args: ValidationResultPartialFragmentNav by lazy { getArgs() }

    override val title by lazy {
        getString(R.string.infschg_result_mask_mandatory_title)
    }
    override val text by lazy {
        getString(R.string.infschg_result_mask_mandatory_copy)
    }
    override val imageRes = R.drawable.status_mask_required

    override val imageInfo1Res = R.drawable.result_calendar
    override val titleInfo1 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_1_title) }
    override val textInfo1 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_1_copy) }

    override val imageInfo2Res = R.drawable.result_invalid_technical_signature
    override val titleInfo2 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_2_title) }
    override val textInfo2 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_2_copy) }

    override val imageInfo3Res = R.drawable.result_cert_vaccination
    override val titleInfo3 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_3_title) }
    override val textInfo3 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_3_copy) }

    override val allowSecondScan by lazy { args.allowSecondCertificate }
    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationImmunityResultSuccessFragmentNav(
    val name: String,
    val transliteratedName: String,
    val birthDate: String,
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationImmunityResultSuccessFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display immunity validation success.
 */
internal class ValidationImmunityResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationImmunityResultSuccessFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_title)
    }
    override val regionText by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_subtitle)
    }
    override val text by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_message)
    }
    override val imageRes = R.drawable.status_immunity_valid

    override val name by lazy { args.name }
    override val transliteratedName by lazy { args.transliteratedName }
    override val birthDate by lazy { args.birthDate }

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationImmunityResultIncompleteFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationImmunityResultIncompleteFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display immunity validation incomplete.
 */
internal class ValidationImmunityResultIncompleteFragment : ValidationResultFragment() {
    private val args: ValidationImmunityResultIncompleteFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_title)
    }
    override val regionText by lazy {
        getString(R.string.validation_check_popup_valid_vaccination_recovery_subtitle)
    }
    override val text by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_copy)
    }
    override val imageRes = R.drawable.status_immunity_incomplete

    override val imageInfo1Res = R.drawable.status_immunity_incomplete_icon2
    override val titleInfo1 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_expiration)
    }
    override val textInfo1 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_expiration_text)
    }

    override val imageInfo2Res = R.drawable.status_immunity_incomplete_icon1
    override val titleInfo2 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_protection)
    }
    override val textInfo2 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_protection_text)
    }

    override val imageInfo3Res = R.drawable.status_immunity_incomplete_icon1
    override val titleInfo3 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted)
    }
    override val textInfo3 by lazy {
        getString(R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted_text)
    }

    override val textFooter = R.string.validation_faq_link

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes =
        R.string.functional_validation_check_popup_unsuccessful_certificate_subheadline_uncompleted_button
}

@Parcelize
internal class ValidationResultFailedFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultFailedFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display immunity validation failed.
 */
internal class ValidationResultFailedFragment : ValidationResultFragment() {
    private val args: ValidationResultFailedFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_title)
    }
    override val regionText = null
    override val text by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_subline)
    }
    override val imageRes = R.drawable.status_immunity_failed

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

    override val imageInfo3Res = R.drawable.result_invalid_technical_time
    override val titleInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_title)
    }
    override val textInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_subtitle)
    }

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationResultFailedSecondCertificateFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultFailedSecondCertificateFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display immunity validation failed.
 */
internal class ValidationResultFailedSecondCertificateFragment : ValidationResultFragment() {
    private val args: ValidationResultFailedSecondCertificateFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_title)
    }
    override val text by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_subline)
    }
    override val imageRes = R.drawable.status_immunity_failed

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

    override val imageInfo3Res = R.drawable.result_invalid_technical_time
    override val titleInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_title)
    }
    override val textInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_subtitle)
    }

    override val buttonTextRes = R.string.technical_validation_check_popup_retry

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
        bottomSheetBinding.bottomSheetSecondWhiteButtonWithBorder.apply {
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationResultClosed()
            }
            setText(R.string.result_2G_button_startover)
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().popUntil<ValidationResultListener>()?.onValidationRetryLastScan()
    }
}

@Parcelize
internal class ValidationEntryResultFailedFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationEntryResultFailedFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display entry validation failed.
 */
internal class ValidationEntryResultFailedFragment : ValidationResultFragment() {
    private val args: ValidationEntryResultFailedFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_title)
    }
    override val regionText = null
    override val text by lazy {
        getString(R.string.technical_validation_check_popup_unsuccessful_certificate_subline)
    }
    override val imageRes = R.drawable.status_immunity_failed

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

    override val imageInfo3Res = R.drawable.result_invalid_technical_time
    override val titleInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_title)
    }
    override val textInfo3 by lazy {
        getString(R.string.technical_validation_invalid_expired_subtitle)
    }

    override val textFooter: Int = R.string.entry_check_link

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationEntryResultSuccessFragmentNav(
    val name: String,
    val transliteratedName: String,
    val birthDate: String,
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationEntryResultSuccessFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display entry validation success.
 */
internal class ValidationEntryResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationEntryResultSuccessFragmentNav by lazy { getArgs() }
    override val title by lazy {
        getString(R.string.entry_check_success_title)
    }
    override val regionText by lazy {
        getString(R.string.entry_check_success_subtitle)
    }
    override val text by lazy {
        getString(R.string.entry_check_success_copy)
    }
    override val imageRes = R.drawable.status_immunity_valid

    override val name by lazy { args.name }
    override val transliteratedName by lazy { args.transliteratedName }
    override val birthDate by lazy { args.birthDate }

    override val textFooter: Int = R.string.entry_check_link

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}
