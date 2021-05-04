package com.ibm.health.vaccination.app.certchecker.validation

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
import com.ibm.health.common.vaccination.app.BaseBottomSheet
import com.ibm.health.common.vaccination.app.utils.formatDateOrEmpty
import com.ibm.health.vaccination.app.certchecker.R
import com.ibm.health.vaccination.app.certchecker.databinding.ValidationResultBinding
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

interface ValidationResultListener {
    fun onValidationResultClosed()
}

abstract class ValidationResultFragment : BaseBottomSheet() {

    override val buttonTextRes: String by lazy { getString(R.string.validation_result_button) }
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
}

@Parcelize
class ValidationResultSuccessFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
) : FragmentNav(ValidationResultSuccessFragment::class)

class ValidationResultSuccessFragment : ValidationResultFragment() {
    private val args: ValidationResultSuccessFragmentNav by lazy { getArgs() }
    override val titleRes = R.string.validation_result_success_title
    override val textRes = R.string.validation_result_success_text
    override val imageRes = R.drawable.result_success_image
    override val titleInfo1String by lazy { args.name }
    override val textInfo1String by lazy {
        getString(R.string.validation_result_birth_date, args.birthDate.formatDateOrEmpty())
    }
    override val imageInfo1Res = R.drawable.result_person
}

@Parcelize
class ValidationResultIncompleteFragmentNav(
    val name: String,
    val birthDate: LocalDate?,
) : FragmentNav(ValidationResultIncompleteFragment::class)

class ValidationResultIncompleteFragment : ValidationResultFragment() {
    private val args: ValidationResultIncompleteFragmentNav by lazy { getArgs() }
    override val titleRes = R.string.validation_result_incomplete_title
    override val textRes = R.string.validation_result_incomplete_text
    override val imageRes = R.drawable.result_incomplete_image
    override val titleInfo1String by lazy { args.name }
    override val textInfo1String by lazy {
        getString(R.string.validation_result_birth_date, args.birthDate.formatDateOrEmpty())
    }
    override val imageInfo1Res = R.drawable.result_person
}

@Parcelize
class ValidationResultFailureFragmentNav : FragmentNav(ValidationResultFailureFragment::class)

class ValidationResultFailureFragment : ValidationResultFragment() {
    override val titleRes = R.string.validation_result_failure_title
    override val textRes = R.string.validation_result_failure_text
    override val imageRes = R.drawable.result_failure_image
    override val titleInfo1String by lazy { getString(R.string.validation_result_failure_info1_title) }
    override val textInfo1String by lazy { getString(R.string.validation_result_failure_info1_text) }
    override val imageInfo1Res = R.drawable.result_search
    override val titleInfo2Res = R.string.validation_result_failure_info2_title
    override val textInfo2Res = R.string.validation_result_failure_info2_text
    override val imageInfo2Res = R.drawable.hourglass
}
