/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.databinding.DgcDetailDataRowBinding
import de.rki.covpass.app.detail.DgcEntryDetailFragment

@SuppressLint("NotifyDataSetChanged")
public class DgcEntryDetailAdapter(
    parent: Fragment,
) : BaseRecyclerViewAdapter<BindingViewHolder<*>>(parent) {

    private var detailItems: List<DgcEntryDetailFragment.DataRow> = emptyList()

    public fun updateList(newResultItems: List<DgcEntryDetailFragment.DataRow>) {
        detailItems = newResultItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<*> =
        DgcDetailViewHolder(parent)

    override fun onBindViewHolder(holder: BindingViewHolder<*>, position: Int) {
        (holder as? DgcDetailViewHolder)?.bind(detailItems[position])
    }

    override fun getItemCount(): Int = detailItems.size

    public inner class DgcDetailViewHolder(parent: ViewGroup) :
        BindingViewHolder<DgcDetailDataRowBinding>(
            parent,
            DgcDetailDataRowBinding::inflate,
        ) {
        public fun bind(item: DgcEntryDetailFragment.DataRow) {
            binding.dgcDetailDataHeaderTextview.text = item.header
            binding.dgcDetailDataHeaderTextview.contentDescription = item.headerAccessibleDescription
            binding.dgcDetailDataTextview.text = item.value

            if (item.description != null) {
                binding.dgcDetailDescriptionTextview.isVisible = true
                binding.dgcDetailDescriptionTextview.text = item.description
            } else {
                binding.dgcDetailDescriptionTextview.isVisible = false
            }

            if (item.valueAccessibleDescription != null) {
                binding.dgcDetailDataTextview.contentDescription = item.valueAccessibleDescription
            }
        }
    }
}
