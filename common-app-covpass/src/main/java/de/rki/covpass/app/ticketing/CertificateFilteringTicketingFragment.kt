/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateFilteringTicketingBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import kotlinx.parcelize.Parcelize

@Parcelize
public class CertificateFilteringTicketingFragmentNav(
    public val ticketingDataInitialization: TicketingDataInitialization,
) : FragmentNav(CertificateFilteringTicketingFragment::class)

public class CertificateFilteringTicketingFragment : BaseBottomSheet(), DialogListener {

    private val binding by viewBinding(CertificateFilteringTicketingBinding::inflate)
    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT

    override val buttonTextRes: Int = R.string.share_certificate_selection_no_match_action_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.share_certificate_title)
        binding.certificateFilteringHeader.setText(R.string.share_certificate_selection_message)
        CertificateFilteringTicketingAdapter(this).attachTo(binding.certificateFilteringCertificatesRecycler)
    }

    override fun onCloseButtonClicked() {
        val dialogModel = DialogModel(
            titleRes = R.string.cancellation_share_certificate_title,
            positiveButtonTextRes = R.string.cancellation_share_certificate_action_button_yes,
            negativeButtonTextRes = R.string.cancellation_share_certificate_action_button_no,
            tag = CANCEL_FILTERING_TICKETING,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onActionButtonClicked() {
        onCloseButtonClicked()
    }

    override fun onClickOutside() {
        onCloseButtonClicked()
    }

    override fun onBackPressed(): Abortable {
        onCloseButtonClicked()
        return Abort
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == CANCEL_FILTERING_TICKETING && action == DialogAction.POSITIVE) {
            findNavigator().popAll()
        }
    }

    // TODO will be removed after merging the initialization
    @Suppress("UnusedPrivateMember")
    private fun updateValidCertificatesList(list: List<CombinedCovCertificate>) {
        binding.certificateFilteringLoadingLayout.isVisible = false
        binding.certificateFilteringData.isVisible = list.isNotEmpty()
        binding.certificateFilteringCertificatesRecycler.isVisible = list.isNotEmpty()
        binding.certificateFilteringEmptyCertificatesLayout.isVisible = list.isEmpty()
        if (list.isNotEmpty()) {
            (binding.certificateFilteringCertificatesRecycler.adapter as CertificateFilteringTicketingAdapter)
                .updateList(list)
        }
    }

    public companion object {
        public const val CANCEL_FILTERING_TICKETING: String = "cancel_filtering_ticketing"
    }
}
