/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ReissueResultPopupContentBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import kotlinx.parcelize.Parcelize

public interface ReissueCallback {
    public fun onReissueCancel()
    public fun onReissueFinish(certificatesId: GroupedCertificatesId?)
}

@Parcelize
public class ReissueResultFragmentNav(
    public val listCertIds: List<String>,
) : FragmentNav(ReissueResultFragment::class)

public class ReissueResultFragment : BaseBottomSheet(), ReissueResultEvents, DialogListener {

    private val args: ReissueResultFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { ReissueResultViewModel(scope, args.listCertIds) }
    private val binding by viewBinding(ReissueResultPopupContentBinding::inflate)
    private val oldCombinedCovCertificate by lazy {
        covpassDeps.certRepository.certs.value.getCombinedCertificate(args.listCertIds[0])
    }
    private var groupedCertificatesId: GroupedCertificatesId? = null
    override val buttonTextRes: Int = R.string.certificate_renewal_confirmation_page_certificate_delete_button
    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingLayout.isVisible = true
        binding.reissueResultLayout.isVisible = false
        bottomSheetBinding.bottomSheetTitle.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = false
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = false
        bottomSheetBinding.bottomSheetTextButton.apply {
            setText(R.string.certificate_renewal_confirmation_page_certificate_secondary_button)
            isVisible = true
            setOnClickListener {
                findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
            }
        }
        bottomSheetBinding.bottomSheetTitle.setText(R.string.certificate_renewal_confirmation_page_headline)
        binding.reissueResultInfo.setText(R.string.certificate_renewal_confirmation_page_copy)
        binding.reissueResultTitleDataElementNew
            .setText(R.string.certificate_renewal_confirmation_page_certificate_list_new)
        binding.reissueResultTitleDataElementOld
            .setText(R.string.certificate_renewal_confirmation_page_certificate_list_old)
        oldCombinedCovCertificate?.covCertificate?.let {
            binding.reissueResultDataElementOld.showCertificate(it, true)
        }
    }

    override fun onActionButtonClicked() {
        viewModel.deleteOldCertificate(args.listCertIds[0])
    }

    override fun onReissueFinish(
        cert: CovCertificate,
        groupedCertificatesId: GroupedCertificatesId
    ) {

        this.groupedCertificatesId = groupedCertificatesId

        binding.reissueResultDataElementNew.showCertificate(cert, false)
        binding.loadingLayout.isVisible = false
        binding.reissueResultLayout.isVisible = true
        bottomSheetBinding.bottomSheetTitle.isVisible = true
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = true
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
    }

    override fun onDeleteOldCertificateFinish() {
        findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
    }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
        return Abort
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
    }

    override fun onClickOutside() {}

    public companion object {
        public const val REISSUE_RESULT_END_PROCESS: String = "reissue_result_end_process"
    }
}
