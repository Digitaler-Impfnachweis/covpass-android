/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getSpanned
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ConsentInitializationDataProtectionItemBinding
import de.rki.covpass.app.databinding.ConsentInitializationInfoboxItemBinding
import de.rki.covpass.app.databinding.ConsentInitializationNoteItemBinding
import de.rki.covpass.app.databinding.ConsentInitializationTicketingDataItemBinding
import de.rki.covpass.app.uielements.setValues
import de.rki.covpass.commonapp.utils.setExternalLinkImage

public class ConsentTicketingAdapter(
    private val items: List<ConsentInitItem>,
    private val fragment: Fragment,
) : BaseRecyclerViewAdapter<BaseConsentInitializationViewHolder<*>>(fragment) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseConsentInitializationViewHolder<*> =
        when (viewType) {
            ITEM_VIEW_TYPE_DATA -> TicketingDataViewHolder(parent)
            ITEM_VIEW_TYPE_NOTE -> NoteViewHolder(parent)
            ITEM_VIEW_TYPE_INFOBOX -> InfoBoxViewHolder(fragment, parent)
            ITEM_VIEW_TYPE_DATA_PROTECTION -> DataProtectionViewHolder(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }

    override fun onBindViewHolder(holder: BaseConsentInitializationViewHolder<*>, position: Int) {
        holder.onItemBind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ConsentInitItem.TicketingData -> ITEM_VIEW_TYPE_DATA
            is ConsentInitItem.Note -> ITEM_VIEW_TYPE_NOTE
            is ConsentInitItem.Infobox -> ITEM_VIEW_TYPE_INFOBOX
            is ConsentInitItem.DataProtection -> ITEM_VIEW_TYPE_DATA_PROTECTION
        }
    }

    private companion object {
        private const val ITEM_VIEW_TYPE_DATA = 0
        private const val ITEM_VIEW_TYPE_NOTE = 1
        private const val ITEM_VIEW_TYPE_INFOBOX = 2
        private const val ITEM_VIEW_TYPE_DATA_PROTECTION = 3
    }
}

public abstract class BaseConsentInitializationViewHolder<B : ViewBinding>(
    parent: ViewGroup,
    inflater: (LayoutInflater, ViewGroup, Boolean) -> B,
) : BindingViewHolder<B>(parent, inflater) {

    public abstract fun onItemBind(item: ConsentInitItem)
}

private class TicketingDataViewHolder(
    parent: ViewGroup,
) : BaseConsentInitializationViewHolder<ConsentInitializationTicketingDataItemBinding>(
    parent,
    ConsentInitializationTicketingDataItemBinding::inflate,
) {

    override fun onItemBind(item: ConsentInitItem) {
        (item as ConsentInitItem.TicketingData).let {
            binding.consentTicketingDataHeader.text = it.header
            binding.consentTicketingDataValue.text = it.value
        }
    }
}

private class NoteViewHolder(
    parent: ViewGroup,
) : BaseConsentInitializationViewHolder<ConsentInitializationNoteItemBinding>(
    parent,
    ConsentInitializationNoteItemBinding::inflate,
) {

    override fun onItemBind(item: ConsentInitItem) {
        (item as ConsentInitItem.Note).let {
            binding.consentInitializationNoteBulletPoint.isVisible = it.bulletPoint
            binding.consentInitializationNoteValue.text = it.text
        }
    }
}

private class InfoBoxViewHolder(
    val fragment: Fragment,
    parent: ViewGroup,
) : BaseConsentInitializationViewHolder<ConsentInitializationInfoboxItemBinding>(
    parent,
    ConsentInitializationInfoboxItemBinding::inflate,
) {

    override fun onItemBind(item: ConsentInitItem) {
        (item as ConsentInitItem.Infobox).let {
            binding.consentTicketingInfoElement.setValues(
                title = item.title,
                subtitle = item.description,
                iconRes = R.drawable.info_icon_update_app,
                backgroundRes = R.drawable.info_background,
                list = item.list,
                parent = fragment,
            )
        }
    }
}

private class DataProtectionViewHolder(
    parent: ViewGroup,
) : BaseConsentInitializationViewHolder<ConsentInitializationDataProtectionItemBinding>(
    parent,
    ConsentInitializationDataProtectionItemBinding::inflate,
) {

    override fun onItemBind(item: ConsentInitItem) {
        (item as ConsentInitItem.DataProtection).let { dataProtection ->
            binding.consentTicketingDataProtectionNote.text = dataProtection.note
            binding.consentTicketingDataProtectionField.apply {
                text = getSpanned(dataProtection.linkTitle, dataProtection.link)
                movementMethod = LinkMovementMethod.getInstance()
                setExternalLinkImage()
            }
        }
    }
}
