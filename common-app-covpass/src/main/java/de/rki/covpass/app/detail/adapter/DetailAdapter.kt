/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DetailBoosterNotificationItemBinding
import de.rki.covpass.app.databinding.DetailDataRowBinding
import de.rki.covpass.app.databinding.DetailFullnameItemBinding
import de.rki.covpass.app.databinding.DetailHeaderItemBinding
import de.rki.covpass.app.databinding.DetailInfoboxRowBinding
import de.rki.covpass.app.databinding.DetailReissueNotificationItemBinding
import de.rki.covpass.app.databinding.DetailWidgetItemBinding
import de.rki.covpass.app.detail.DetailClickListener
import de.rki.covpass.commonapp.utils.stripUnderlinesAndSetExternalLinkImage
import de.rki.covpass.sdk.cert.models.DGCEntryType

/**
 * Adapter which holds the data for Detail screen.
 * Holds all possible [DGCEntryType]'s
 */
internal class DetailAdapter(
    private val items: List<DetailItem>,
    private val listener: DetailClickListener,
    parent: Fragment,
) : BaseRecyclerViewAdapter<BaseViewHolder<*>>(parent) {

    private companion object {
        private const val ITEM_VIEW_TYPE_NAME = 0
        private const val ITEM_VIEW_TYPE_WIDGET = 1
        private const val ITEM_VIEW_TYPE_HEADER = 2
        private const val ITEM_VIEW_TYPE_PERSONAL = 3
        private const val ITEM_VIEW_TYPE_INFOBOX = 4
        private const val ITEM_VIEW_TYPE_CERTIFICATE = 5
        private const val ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION = 6
        private const val ITEM_VIEW_TYPE_REISSUE_NOTIFICATION = 7
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ITEM_VIEW_TYPE_NAME -> FullnameViewHolder(parent)
            ITEM_VIEW_TYPE_WIDGET -> WidgetViewHolder(parent, listener)
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(parent)
            ITEM_VIEW_TYPE_PERSONAL -> PersonalDataViewHolder(parent)
            ITEM_VIEW_TYPE_INFOBOX -> InfoboxViewHolder(parent)
            ITEM_VIEW_TYPE_CERTIFICATE -> CertificateViewHolder(parent, listener)
            ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION -> BoosterNotificationViewHolder(parent)
            ITEM_VIEW_TYPE_REISSUE_NOTIFICATION -> ReissueNotificationViewHolder(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        holder.onItemBind(items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DetailItem.Name -> ITEM_VIEW_TYPE_NAME
            is DetailItem.Widget -> ITEM_VIEW_TYPE_WIDGET
            is DetailItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DetailItem.Personal -> ITEM_VIEW_TYPE_PERSONAL
            is DetailItem.Infobox -> ITEM_VIEW_TYPE_INFOBOX
            is DetailItem.Certificate -> ITEM_VIEW_TYPE_CERTIFICATE
            is DetailItem.BoosterNotification -> ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION
            is DetailItem.ReissueNotification -> ITEM_VIEW_TYPE_REISSUE_NOTIFICATION
        }
    }
}

private class FullnameViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailFullnameItemBinding>(parent, DetailFullnameItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Name).let {
            binding.detailNameTextview.text = it.fullname
        }
    }
}

private class WidgetViewHolder(
    parent: ViewGroup,
    private val listener: DetailClickListener,
) : BaseViewHolder<DetailWidgetItemBinding>(parent, DetailWidgetItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        ViewCompat.setScreenReaderFocusable(binding.root, false)
        (item as DetailItem.Widget).let { widget ->
            if (item.isOneElementForScreenReader) {
                ViewCompat.setScreenReaderFocusable(binding.detailInfoLayoutForAccessibility, true)
            } else {
                ViewCompat.setScreenReaderFocusable(binding.detailInfoLayout, true)
                ViewCompat.setScreenReaderFocusable(binding.detailStatusTextview, true)
            }
            binding.detailInfoLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            binding.detailStatusHeaderTextview.text = widget.title
            binding.detailStatusImageview.setImageResource(widget.statusIcon)
            binding.detailStatusTextview.text = widget.message

            widget.subtitle?.let {
                binding.detailStatusSubheaderTextview.isVisible = true
                binding.detailStatusSubheaderTextview.text = it
            }

            widget.region?.let {
                binding.detailRegionSubheaderTextview.isVisible = true
                binding.detailRegionSubheaderTextview.text = it
            }

            widget.link?.let {
                binding.detailStatusLinkTextview.isVisible = true
                binding.detailStatusLinkTextview.apply {
                    text = getSpanned(it)
                    movementMethod = LinkMovementMethod.getInstance()
                    stripUnderlinesAndSetExternalLinkImage()
                }
            }

            widget.noticeMessage?.let {
                ViewCompat.setScreenReaderFocusable(binding.detailNoticeTitleTextview, true)
                ViewCompat.setScreenReaderFocusable(binding.detailNoticeSubheaderTextview, true)
                binding.detailNoticeTitleTextview.isVisible = true
                binding.detailNoticeTitleLayout.setOnClickListener {
                    showInfoContainer(!binding.detailInfoContentContainer.isVisible)
                }
                binding.detailNoticeSubheaderTextview.isVisible = true
                binding.detailNoticeSubheaderTextview.text = it
                binding.changeFederalStateButton.isVisible = true
                binding.changeFederalStateButton.setOnClickListener {
                    listener.onChangeFederalStateClicked()
                }
            }
            binding.detailInfoContainer.isVisible = widget.noticeMessage != null

            widget.buttonText?.let {
                binding.detailShowCertificateButton.isVisible = true
                binding.detailShowCertificateButton.text = it
                binding.detailShowCertificateButton.setOnClickListener {
                    if (widget.isExpiredOrInvalid) {
                        listener.onNewCertificateScanClicked()
                    } else {
                        listener.onShowCertificateClicked()
                    }
                }
            }
        }
    }
    private fun showInfoContainer(isVisible: Boolean) {
        binding.detailInfoContentContainer.isVisible = isVisible
        val arrow = if (isVisible) R.drawable.arrow_up_blue else R.drawable.arrow_down_blue
        binding.detailNoticeTitleArrow.setImageResource(arrow)
    }
}

private class HeaderViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailHeaderItemBinding>(parent, DetailHeaderItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Header).let {
            binding.detailPersonalHeaderTextview.text = it.title
            binding.detailPersonalHeaderTextview.contentDescription = it.titleAccessibleDescription
        }
    }
}

private class PersonalDataViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailDataRowBinding>(parent, DetailDataRowBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Personal).let {
            binding.detailDataHeaderTextview.text = it.title
            binding.detailDataHeaderTextview.contentDescription = it.titleAccessibleDescription
            binding.detailDataTextview.text = it.subtitle
        }
    }
}

private class InfoboxViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailInfoboxRowBinding>(parent, DetailInfoboxRowBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Infobox).let {
            binding.detailInfoboxElement.title = it.title
            binding.detailInfoboxElement.descriptionNoLink = it.description
        }
    }
}

private class ReissueNotificationViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailReissueNotificationItemBinding>(
    parent,
    DetailReissueNotificationItemBinding::inflate,
) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.ReissueNotification).let {
            binding.reissueNotificationTitle.text = getString(it.titleRes)
            binding.reissueNotificationText.text = it.textRes
            binding.reissueNotificationButton.setText(it.buttonRes)
            binding.reissueNotificationButton.isVisible = it.isButtonVisible
            binding.reissueNotificationButton.setOnClickListener(it.buttonClickListener)
        }
    }
}

private class BoosterNotificationViewHolder(
    val parent: ViewGroup,
) : BaseViewHolder<DetailBoosterNotificationItemBinding>(
    parent,
    DetailBoosterNotificationItemBinding::inflate,
) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.BoosterNotification).let {
            binding.notificationTitle.text = getString(it.titleRes)
            binding.notificationText.text = getString(
                R.string.vaccination_certificate_overview_booster_vaccination_notification_message,
                it.description,
                it.ruleId,
            )
            binding.notificationFaq.apply {
                text = getSpanned(R.string.vaccination_certificate_overview_faqlink)
                movementMethod = LinkMovementMethod.getInstance()
                stripUnderlinesAndSetExternalLinkImage()
            }
            it.iconTextRes?.let { textIcon -> binding.notificationIcon.text = getString(textIcon) }
            it.iconBackgroundRes?.let { iconRes ->
                binding.notificationIcon.background = getDrawable(parent.context, iconRes)
            }
        }
    }
}

public abstract class BaseViewHolder<B : ViewBinding>(
    parent: ViewGroup,
    inflater: (LayoutInflater, ViewGroup, Boolean) -> B,
) : BindingViewHolder<B>(parent, inflater) {

    public abstract fun onItemBind(item: DetailItem)
}

internal fun ImageView.setTint(@ColorRes color: Int) {
    drawable.invalidateSelf()
    val wrapDrawable = DrawableCompat.wrap(drawable).mutate()
    DrawableCompat.setTint(
        wrapDrawable,
        ContextCompat.getColor(context, color),
    )
}

public fun View.setLayoutBackgroundColor(@ColorRes color: Int) {
    setBackgroundColor(
        ContextCompat.getColor(context, color),
    )
}
