/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.federalstate

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.checkapp.databinding.ChangeFederalStateItemBinding
import de.rki.covpass.commonapp.utils.FederalState

/**
 * Adapter which holds the list of countries for [ChangeFederalStateFragment].
 */
@SuppressLint("NotifyDataSetChanged")
public class ChangeFederalStateAdapter(
    parent: Fragment,
    private val startRegionId: String,
    private val listener: (position: Int) -> Unit,
) : BaseRecyclerViewAdapter<ChangeFederalStateAdapter.ChangeFederalStateViewHolder>(parent) {

    private var federalStateItems: List<FederalState> = emptyList()

    public fun updateList(newFederalStateItems: List<FederalState>) {
        federalStateItems = newFederalStateItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeFederalStateViewHolder =
        ChangeFederalStateViewHolder(parent)

    override fun onBindViewHolder(holder: ChangeFederalStateViewHolder, position: Int) {
        holder.bind(federalStateItems[position])
    }

    public fun getItem(position: Int): FederalState = federalStateItems[position]

    override fun getItemCount(): Int = federalStateItems.size

    public inner class ChangeFederalStateViewHolder(parent: ViewGroup) :
        BindingViewHolder<ChangeFederalStateItemBinding>(
            parent,
            ChangeFederalStateItemBinding::inflate,
        ),
        View.OnClickListener {

        init {
            binding.federalStateLayout.setOnClickListener(this)
        }

        public fun bind(item: FederalState) {
            binding.federalStateName.setText(item.nameRes)

            if (item.regionId == startRegionId) {
                binding.federalStateLayout.requestFocus()
            }
        }

        override fun onClick(p0: View?) {
            listener.invoke(adapterPosition)
        }
    }
}
