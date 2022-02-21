/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ConsentInitializationTicketingBinding
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import kotlinx.parcelize.Parcelize

@Parcelize
public class ConsentInitializationTicketingFragmentNav(
    public val ticketingDataInitialization: TicketingDataInitialization,
) : FragmentNav(ConsentInitializationTicketingFragment::class)

public class ConsentInitializationTicketingFragment : BaseTicketingFragment() {

    private val binding by viewBinding(ConsentInitializationTicketingBinding::inflate)
    private val viewModel by reactiveState { ConsentInitializationTicketingViewModel(scope) }

    public val args: ConsentInitializationTicketingFragmentNav by lazy { getArgs() }

    override val announcementAccessibilityRes: Int? = null // TODO change to correct text

    override val buttonTextRes: Int = R.string.share_certificate_transmission_action_button_agree

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.share_certificate_title)
        bottomSheetBinding.bottomSheetCancelButton.apply {
            isVisible = true
            setText(R.string.share_certificate_action_button_cancel)
            setOnClickListener {
                onCloseButtonClicked()
            }
        }

        updateView(
            args.ticketingDataInitialization.serviceProvider,
            args.ticketingDataInitialization.subject,
            args.ticketingDataInitialization.privacyUrl
        )
    }

    override fun onActionButtonClicked() {
        validateServiceIdentity()
    }

    private fun validateServiceIdentity() {
        if (viewModel.isWhitelisted(args.ticketingDataInitialization.serviceIdentity)) {
            findNavigator().push(CertificateFilteringTicketingFragmentNav(args.ticketingDataInitialization))
        } else {
            findNavigator().push(UnknownProviderTicketingFragmentNav(args.ticketingDataInitialization))
        }
    }

    private fun updateView(provider: String, booking: String, privacyUrl: String) {
        val list = mutableListOf(
            ConsentInitItem.TicketingData(
                getString(R.string.share_certificate_transmission_details_provider),
                provider
            ),
            ConsentInitItem.TicketingData(
                getString(R.string.share_certificate_transmission_details_booking),
                booking
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_message)
            ),
            ConsentInitItem.Infobox(
                getString(R.string.share_certificate_consent_title),
                getString(R.string.share_certificate_consent_message),
                listOf(
                    getString(R.string.share_certificate_consent_first_list_item, provider),
                    getString(R.string.share_certificate_consent_second_list_item),
                )
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_notes_first_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_notes_second_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_notes_third_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_notes_fourth_list_item),
                true
            ),
            ConsentInitItem.DataProtection(
                getString(R.string.share_certificate_note_privacy_notice),
                R.string.share_certificate_privacy_notice_linked,
                privacyUrl
            )
        )
        ConsentTicketingAdapter(list, this).attachTo(binding.consentInitializationRecyclerView)
    }
}
