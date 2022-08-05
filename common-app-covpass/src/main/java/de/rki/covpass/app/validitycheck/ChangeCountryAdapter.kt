/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validitycheck

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ChangeCountryHeaderBinding
import de.rki.covpass.app.databinding.ChangeCountryItemBinding
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.commonapp.utils.stripUnderlines

/**
 * Adapter which holds the list of countries for [ChangeCountryFragment].
 */
@SuppressLint("NotifyDataSetChanged")
public class ChangeCountryAdapter(
    parent: Fragment,
    private val startCountry: String,
    private val accessibilityCallback: AccessibilityCallback,
) : BaseRecyclerViewAdapter<BindingViewHolder<*>>(parent) {

    private var countryItems: List<Country> = emptyList()
    private var checked = -1

    public fun getSelectedItem(): Country = countryItems[checked - 1]

    public fun updateList(newCountryItems: List<Country>) {
        countryItems = newCountryItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<*> = when (viewType) {
        TYPE_HEADER -> HeaderChangeCountryViewHolder(parent)
        TYPE_NORMAL -> ChangeCountryViewHolder(parent)
        else -> ChangeCountryViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BindingViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                (holder as? HeaderChangeCountryViewHolder)?.bind()
            }
            TYPE_NORMAL -> {
                (holder as? ChangeCountryViewHolder)?.bind(countryItems[position - 1], position)
            }
        }
    }

    override fun getItemCount(): Int = countryItems.size + 1

    override fun getItemViewType(position: Int): Int = if (position == 0) {
        TYPE_HEADER
    } else {
        TYPE_NORMAL
    }

    public inner class HeaderChangeCountryViewHolder(parent: ViewGroup) :
        BindingViewHolder<ChangeCountryHeaderBinding>(
            parent,
            ChangeCountryHeaderBinding::inflate,
        ) {

        public fun bind() {
            binding.noteCountry.apply {
                text = getSpanned(R.string.certificate_check_validity_selection_country_note)
                movementMethod = LinkMovementMethod.getInstance()
                stripUnderlines()
            }
        }
    }

    public inner class ChangeCountryViewHolder(parent: ViewGroup) :
        BindingViewHolder<ChangeCountryItemBinding>(
            parent,
            ChangeCountryItemBinding::inflate,
        ) {

        public fun bind(item: Country, position: Int) {
            binding.countryName.setText(item.nameRes)
            binding.countryImage.setImageResource(item.flag)
            if (checked == -1) {
                if (startCountry == item.countryCode) {
                    binding.checkbox.isChecked = true
                    checked = position
                }
            } else {
                binding.checkbox.isChecked = position == checked
            }
            binding.layoutCountry.setOnClickListener {
                binding.checkbox.isChecked = true
                checked = position
                notifyDataSetChanged()
                accessibilityCallback.updateAccessibilityFocus()
            }
            binding.root.setAccessibilityDelegate(
                object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = CheckBox::class.java.name
                        if (binding.checkbox.isChecked) {
                            info.contentDescription = getString(
                                R.string.accessibility_certificate_check_validity_selection_country_selected,
                                getString(item.nameRes),
                            )
                        } else {
                            info.contentDescription = getString(
                                R.string.accessibility_certificate_check_validity_selection_country_unselected,
                                getString(item.nameRes),
                            )
                        }
                    }
                },
            )
        }
    }

    private companion object {
        const val TYPE_HEADER = 1
        const val TYPE_NORMAL = 2
    }
}
