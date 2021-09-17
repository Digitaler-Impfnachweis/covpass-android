/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validityresult

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.databinding.ResultInfoElementBinding
import de.rki.covpass.commonapp.uielements.showError
import de.rki.covpass.commonapp.uielements.showWarning

@SuppressLint("NotifyDataSetChanged")
public class InfoElementAdapter(
    parent: Fragment
) : BaseRecyclerViewAdapter<InfoElementAdapter.InfoElementViewHolder>(parent) {

    private var validationResults: List<DerivedValidationResult> = emptyList()

    public fun updateList(newValidationResults: List<DerivedValidationResult>) {
        validationResults = newValidationResults
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoElementViewHolder =
        InfoElementViewHolder(parent)

    override fun onBindViewHolder(holder: InfoElementViewHolder, position: Int) {
        val item = validationResults[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = validationResults.size

    public inner class InfoElementViewHolder(parent: ViewGroup) :
        BindingViewHolder<ResultInfoElementBinding>(
            parent,
            ResultInfoElementBinding::inflate
        ) {

        public fun bind(item: DerivedValidationResult) {
            when (item.result) {
                LocalResult.FAIL -> {
                    binding.resultRowWarning.showError(item.description)
                }
                LocalResult.OPEN -> {
                    binding.resultRowWarning.showWarning(item.description)
                }
                else -> {}
            }
        }
    }
}
