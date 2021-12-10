/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.UnknownProviderTicketingBinding
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import kotlinx.parcelize.Parcelize

@Parcelize
public class UnknownProviderTicketingFragmentNav(
    public val ticketingDataInitialization: TicketingDataInitialization,
) : FragmentNav(UnknownProviderTicketingFragment::class)

public class UnknownProviderTicketingFragment : BaseTicketingFragment() {

    private val binding by viewBinding(UnknownProviderTicketingBinding::inflate)

    public val args: UnknownProviderTicketingFragmentNav by lazy { getArgs() }

    override val announcementAccessibilityRes: Int? = null // TODO change to correct text

    override val buttonTextRes: Int = R.string.warning_unknown_provider_box_button_primary

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.warning_unknown_provider_title)
        bottomSheetBinding.bottomSheetCancelButton.apply {
            isVisible = true
            setText(R.string.warning_unknown_provider_box_button_secondary)
            setOnClickListener {
                onCloseButtonClicked()
            }
        }

        updateView(args.ticketingDataInitialization.serviceProvider)
    }

    override fun onActionButtonClicked() {
        findNavigator().push(CertificateFilteringTicketingFragmentNav(args.ticketingDataInitialization))
    }

    override fun onBackPressed(): Abortable {
        findNavigator().pop()
        return Abort
    }

    private fun updateView(provider: String) {
        val list = mutableListOf(
            ConsentInitItem.Note(
                getString(R.string.warning_unknown_provider_copy, provider)
            ),
            ConsentInitItem.Infobox(
                title = getString(R.string.warning_unknown_provider_box_title),
                list = listOf(
                    getString(R.string.warning_unknown_provider_box_item_1),
                    getString(R.string.warning_unknown_provider_box_item_2),
                    getString(R.string.warning_unknown_provider_box_item_3),
                    getString(R.string.warning_unknown_provider_box_item_4),
                )
            )
        )
        ConsentTicketingAdapter(list, this).attachTo(binding.unknownProviderRecyclerView)
    }
}
