/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.content.Intent
import android.net.Uri
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
import de.rki.covpass.app.databinding.ReissueExpiredResultPopupContentBinding
import de.rki.covpass.app.errorhandling.ReissueErrorHandler.Companion.TAG_ERROR_REISSUING_INTERNAL_ERROR
import de.rki.covpass.app.errorhandling.ReissueErrorHandler.Companion.TAG_ERROR_REISSUING_INTERNAL_SERVER_ERROR
import de.rki.covpass.app.errorhandling.ReissueErrorHandler.Companion.TAG_ERROR_REISSUING_TOO_MANY_REQUESTS
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.ReissueType
import kotlinx.parcelize.Parcelize

@Parcelize
public class ReissueExpiredResultFragmentNav(
    public val listCertIds: List<String>,
    public val reissueType: ReissueType,
) : FragmentNav(ReissueExpiredResultFragment::class)

public class ReissueExpiredResultFragment : ReissueBaseFragment(), ReissueResultEvents, DialogListener {

    private val args: ReissueExpiredResultFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState {
        ReissueResultViewModel(
            scope,
            args.listCertIds,
            args.reissueType,
        )
    }
    private val binding by viewBinding(ReissueExpiredResultPopupContentBinding::inflate)
    private var groupedCertificatesId: GroupedCertificatesId? = null
    override val buttonTextRes: Int = R.string.renewal_expiry_success_button
    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loadingLayout.isVisible = true
        binding.reissueResultLayout.isVisible = false
        bottomSheetBinding.bottomSheetTitle.isVisible = false
        bottomSheetBinding.bottomSheetClose.isVisible = false
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = false
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = false
        binding.reissueResultInfo.setText(R.string.renewal_expiry_success_copy)
    }

    override fun onActionButtonClicked() {
        viewModel.deleteOldCertificate(args.listCertIds[0])
    }

    override fun onReissueFinish(
        cert: CovCertificate,
        groupedCertificatesId: GroupedCertificatesId,
    ) {
        this.groupedCertificatesId = groupedCertificatesId

        binding.loadingLayout.isVisible = false
        binding.reissueResultLayout.isVisible = true
        bottomSheetBinding.bottomSheetBottomLayout.isVisible = true
        bottomSheetBinding.bottomSheetExtraButtonLayout.isVisible = true
    }

    override fun onDeleteOldCertificateFinish() {
        findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
    }

    override fun onBackPressed(): Abortable {
        if (bottomSheetBinding.bottomSheetActionButton.isVisible) {
            onActionButtonClicked()
        } else {
            findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
        }
        return Abort
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (
            action == DialogAction.NEGATIVE && isReissueErrorTag(tag)
        ) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.covpass_reissuing_faq_link)),
                ),
            )
        }
        findNavigator().popUntil<ReissueCallback>()?.onReissueFinish(groupedCertificatesId)
    }

    private fun isReissueErrorTag(tag: String): Boolean =
        tag == TAG_ERROR_REISSUING_INTERNAL_SERVER_ERROR ||
            tag == TAG_ERROR_REISSUING_TOO_MANY_REQUESTS ||
            tag == TAG_ERROR_REISSUING_INTERNAL_ERROR

    override fun onClickOutside() {}
}
