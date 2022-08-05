/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.revocation

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.checkapp.databinding.RevocationExportDetailItemBinding

@SuppressLint("NotifyDataSetChanged")
public class RevocationExportDetailAdapter(
    parent: Fragment,
) : BaseRecyclerViewAdapter<RevocationExportDetailAdapter.RevocationExportDetailViewHolder>(parent) {

    private lateinit var items: List<RevocationExportDetailItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevocationExportDetailViewHolder {
        return RevocationExportDetailViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RevocationExportDetailViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(
        items: List<RevocationExportDetailItem>,
    ) {
        this.items = items
        notifyDataSetChanged()
    }

    public inner class RevocationExportDetailViewHolder(parent: ViewGroup) :
        BindingViewHolder<RevocationExportDetailItemBinding>(
            parent,
            RevocationExportDetailItemBinding::inflate,
        ) {
        public fun bind(item: RevocationExportDetailItem) {
            binding.revocationExportDetailHeaderTextview.setText(item.title)
            binding.revocationExportDetailTextview.text = item.value
        }
    }
}

public data class RevocationExportDetailItem(
    val title: Int,
    val value: String,
)
