/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.importcertificate

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.databinding.ImportCertificateResultElementBinding

@SuppressLint("NotifyDataSetChanged")
public class ImportCertificateSelectorAdapter(
    parent: Fragment,
    private val event: () -> Unit = {}
) : BaseRecyclerViewAdapter<ImportCertificateSelectorAdapter.ImportCertificateResultViewHolder>(parent) {

    private var items: List<ImportCovCertificate> = emptyList()
    public val checkedList: MutableList<ImportCovCertificate> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImportCertificateResultViewHolder {
        return ImportCertificateResultViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ImportCertificateResultViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun getSelectedItems(): List<ImportCovCertificate> = checkedList

    public fun updateList(
        items: List<ImportCovCertificate>,
    ) {
        checkedList.addAll(items)
        this.items = items
        notifyDataSetChanged()
    }

    public fun selectAll() {
        checkedList.removeAll(items)
        checkedList.addAll(items)
        notifyDataSetChanged()
    }

    public fun unselectAll() {
        checkedList.removeAll(items)
        notifyDataSetChanged()
    }

    public inner class ImportCertificateResultViewHolder(parent: ViewGroup) :
        BindingViewHolder<ImportCertificateResultElementBinding>(
            parent,
            ImportCertificateResultElementBinding::inflate
        ) {
        public fun bind(item: ImportCovCertificate) {
            with(binding) {
                elementCertificateDataWithCheckbox.showCertificate(item.covCertificate)
                elementCertificateDataWithCheckbox.changeCheckbox(checkedList.contains(item))
                certLayout.setOnClickListener {
                    if (elementCertificateDataWithCheckbox.isChecked()) {
                        elementCertificateDataWithCheckbox.changeCheckbox(false)
                        checkedList.remove(item)
                    } else {
                        elementCertificateDataWithCheckbox.changeCheckbox(true)
                        checkedList.add(item)
                    }
                    event()
                    notifyDataSetChanged()
                }
            }
        }
    }
}
