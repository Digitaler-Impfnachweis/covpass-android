/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.databinding.ReissueConsentAdapterItemBinding
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate

@SuppressLint("NotifyDataSetChanged")
public class ReissueContentAdapter(
    parent: Fragment
) : BaseRecyclerViewAdapter<ReissueContentAdapter.ReissueConsentViewHolder>(parent) {

    private lateinit var items: List<CombinedCovCertificate>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReissueConsentViewHolder {
        return ReissueConsentViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ReissueConsentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(
        items: List<CombinedCovCertificate>,
    ) {
        this.items = items
        notifyDataSetChanged()
    }

    public inner class ReissueConsentViewHolder(parent: ViewGroup) :
        BindingViewHolder<ReissueConsentAdapterItemBinding>(
            parent,
            ReissueConsentAdapterItemBinding::inflate
        ) {
        public fun bind(item: CombinedCovCertificate) {
            binding.reissueConsentAdapterDataElement.showCertificate(item.covCertificate)
        }
    }
}
