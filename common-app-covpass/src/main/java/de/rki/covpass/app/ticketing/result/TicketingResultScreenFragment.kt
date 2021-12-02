/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing.result

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.TicketingResultScreenBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingPortalValidationResponseResult
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TicketingResultScreenFragmentNav(
    val certId: String,
    val ticketingDataInitialization: TicketingDataInitialization,
    val bookingValidationResponse: BookingValidationResponse,
    val validationServiceId: String,
) : FragmentNav(TicketingResultScreenFragment::class)

internal class TicketingResultScreenFragment : BaseBottomSheet() {

    private val binding by viewBinding(TicketingResultScreenBinding::inflate)
    val args: TicketingResultScreenFragmentNav by lazy { getArgs() }
    override val buttonTextRes = R.string.accessibility_share_certificate_transmission_label_close
    override val heightLayoutParams: Int = ViewGroup.LayoutParams.MATCH_PARENT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheet.setOnClickListener(null)
        startView()
    }

    private fun startView() {
        when (args.bookingValidationResponse.result) {
            BookingPortalValidationResponseResult.OK -> {
                binding.ticketingResultImage.setImageResource(R.drawable.ticketing_result_success)
                binding.ticketingResultTitle.setText(R.string.share_certificate_detail_view_requirements_met_title)
                binding.ticketingResultSubtitle.text = getString(
                    R.string.share_certificate_detail_view_requirements_met_subline,
                    args.validationServiceId
                )
                binding.ticketingResultDescription.text = getString(
                    R.string.share_certificate_detail_view_requirements_met_message,
                    args.ticketingDataInitialization.serviceProvider
                )
            }
            BookingPortalValidationResponseResult.NOK -> {
                binding.ticketingResultImage.setImageResource(R.drawable.ticketing_result_fail)
                binding.ticketingResultTitle.setText(R.string.share_certificate_detail_view_requirements_not_met_title)
                binding.ticketingResultSubtitle.text = getString(
                    R.string.share_certificate_detail_view_requirements_not_met_subline,
                    args.validationServiceId
                )
                binding.ticketingResultDescription.text = getString(
                    R.string.share_certificate_detail_view_requirements_not_met_message,
                    args.ticketingDataInitialization.serviceProvider
                )
                binding.ticketingResultRecyclerview.isVisible = true
                TicketingResultScreenAdapter(this).attachTo(binding.ticketingResultRecyclerview)
                TicketingResultScreenAdapter(this).apply {
                    updateList(args.bookingValidationResponse.resultValidations)
                    attachTo(binding.ticketingResultRecyclerview)
                }
            }
            BookingPortalValidationResponseResult.CHK -> {
                binding.ticketingResultImage.setImageResource(R.drawable.ticketing_result_open)
                binding.ticketingResultTitle
                    .setText(R.string.share_certificate_detail_view_requirements_not_verifiable_title)
                binding.ticketingResultSubtitle.text = getString(
                    R.string.share_certificate_detail_view_requirements_not_verifiable_subline,
                    args.validationServiceId
                )
                binding.ticketingResultDescription.text = getString(
                    R.string.share_certificate_detail_view_requirements_not_verifiable_message,
                    args.ticketingDataInitialization.serviceProvider
                )
                binding.ticketingResultRecyclerview.isVisible = true
                TicketingResultScreenAdapter(this).apply {
                    updateList(args.bookingValidationResponse.resultValidations)
                    attachTo(binding.ticketingResultRecyclerview)
                }
            }
        }
    }

    override fun onActionButtonClicked() {
        findNavigator().popAll()
    }

    override fun onCloseButtonClicked() {
        findNavigator().popAll()
    }

    override fun onClickOutside() {
        findNavigator().popAll()
    }
}
