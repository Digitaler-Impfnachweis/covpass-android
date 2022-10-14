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
import de.rki.covpass.checkapp.revocation.RevocationExportFragmentNav
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.utils.FederalStateResolver
import de.rki.covpass.sdk.cert.models.ExpertModeData
import kotlinx.parcelize.Parcelize

/**
 * Interface to get notified when the validation result fragment was closed.
 */
internal interface ValidationResultListener {
    fun onValidationFirstScanFinish()
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

    open val textFooter: String? = null

    private val expertModeVisible: Boolean by lazy {
        commonDeps.checkContextRepository.isExpertModeOn.value && expertModeData != null
    }

    private val regionText: String by lazy {
        getString(
            R.string.infschg_result_mask_optional_subtitle,
            FederalStateResolver.getFederalStateByCode(
                commonDeps.federalStateRepository.federalState.value,
            )?.nameRes?.let {
                getString(
                    it,
                )
            },
        )
    }

    open val allowSecondScan = false
    open val isGermanCertificate: Boolean = false
    open val expertModeData: ExpertModeData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allowSecondScan) {
            binding.resultLayoutSecondScan.isVisible = true
            binding.bottomSheetActionButton.setOnClickListener {
                findNavigator().popUntil<ValidationResultListener>()?.onValidationFirstScanFinish()
            }
        }
        binding.resultImage.setImageResource(imageRes)
        binding.resultTitle.text = title
        binding.resultText.text = text
        binding.resultRegionText.text = regionText
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

        binding.resultInfoFooter.isVisible = textFooter != null
        if (textFooter != null) {
            binding.resultInfoFooter.text = textFooter
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
internal class ValidationResultInvalidFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultInvalidFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultInvalidFragment : ValidationResultFragment() {
    private val args: ValidationResultInvalidFragmentNav by lazy { getArgs() }

    override val title by lazy { getString(R.string.infschg_result_mask_mandatory_title) }
    override val text by lazy { getString(R.string.infschg_result_mask_mandatory_copy) }
    override val imageRes = R.drawable.status_mask_required

    override val imageInfo1Res = R.drawable.result_invalid_technical_signature
    override val titleInfo1 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_4_title) }
    override val textInfo1 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_4_copy) }

    override val imageInfo2Res = R.drawable.result_invalid_technical_qr
    override val titleInfo2 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_5_title) }
    override val textInfo2 by lazy { getString(R.string.infschg_result_mask_mandatory_reason_5_copy) }

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}

@Parcelize
internal class ValidationResultNoRulesFragmentNav(
    val expertModeData: ExpertModeData?,
    val isGermanCertificate: Boolean = false,
) : FragmentNav(ValidationResultNoRulesFragment::class)

/**
 * Overrides the texts and icons from [ValidationResultFragment] to display validation success.
 */
internal class ValidationResultNoRulesFragment : ValidationResultFragment() {
    private val args: ValidationResultNoRulesFragmentNav by lazy { getArgs() }

    override val title by lazy {
        getString(R.string.infschg_result_no_mask_rules_title)
    }
    override val subtitle: String? by lazy {
        getString(R.string.infschg_result_no_mask_rules_copy_1_bold)
    }
    override val text by lazy {
        getString(R.string.infschg_result_no_mask_rules_copy_2)
    }
    override val imageRes = R.drawable.status_mask_no_rules

    override val isGermanCertificate: Boolean by lazy { args.isGermanCertificate }
    override val expertModeData: ExpertModeData? by lazy { args.expertModeData }

    override val buttonTextRes = R.string.result_2G_button_startover
}
