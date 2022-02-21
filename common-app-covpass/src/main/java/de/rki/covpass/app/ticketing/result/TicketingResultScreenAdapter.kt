/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing.result

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.TicketingResultScreenListItemBinding
import de.rki.covpass.sdk.ticketing.data.validate.BookingPortalValidationResponseResult
import de.rki.covpass.sdk.ticketing.data.validate.BookingPortalValidationResponseResultItem

public class TicketingResultScreenAdapter(
    parent: Fragment,
) : BaseRecyclerViewAdapter<TicketingResultScreenAdapter.TicketingResultScreenViewHolder>(parent) {

    private var list = emptyList<BookingPortalValidationResponseResultItem>()

    public fun updateList(newList: List<BookingPortalValidationResponseResultItem>) {
        list = newList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketingResultScreenViewHolder =
        TicketingResultScreenViewHolder(parent)

    override fun onBindViewHolder(holder: TicketingResultScreenViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    public class TicketingResultScreenViewHolder(
        parent: ViewGroup,
    ) : BindingViewHolder<TicketingResultScreenListItemBinding>(
        parent,
        TicketingResultScreenListItemBinding::inflate
    ) {
        public fun bind(item: BookingPortalValidationResponseResultItem) {
            binding.ticketingResultItemImage.setImageResource(
                if (item.result == BookingPortalValidationResponseResult.NOK) {
                    R.drawable.ticketing_item_icon_fail
                } else {
                    R.drawable.ticketing_item_icon_open
                }
            )
            binding.ticketingResultItemText.text = item.details
        }
    }
}
