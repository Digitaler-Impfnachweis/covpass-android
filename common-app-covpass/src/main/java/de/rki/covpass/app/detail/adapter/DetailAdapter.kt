/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.*
import de.rki.covpass.app.detail.DetailClickListener
import de.rki.covpass.sdk.cert.models.*

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
        private const val ITEM_VIEW_TYPE_CERTIFICATE = 4
        private const val ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION = 5
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            ITEM_VIEW_TYPE_NAME -> FullnameViewHolder(parent)
            ITEM_VIEW_TYPE_WIDGET -> WidgetViewHolder(parent, listener)
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(parent)
            ITEM_VIEW_TYPE_PERSONAL -> PersonalDataViewHolder(parent)
            ITEM_VIEW_TYPE_CERTIFICATE -> CertificateViewHolder(parent, listener)
            ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION -> NotificationViewHolder(parent)
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
            is DetailItem.Certificate -> ITEM_VIEW_TYPE_CERTIFICATE
            is DetailItem.Notification -> ITEM_VIEW_TYPE_BOOSTER_NOTIFICATION
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
        (item as DetailItem.Widget).let { widget ->
            binding.detailStatusHeaderTextview.text = widget.title
            binding.detailStatusImageview.setImageResource(widget.statusIcon)
            binding.detailStatusTextview.text = widget.message
            binding.detailShowCertificateButton.text = widget.buttonText
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

private class HeaderViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailHeaderItemBinding>(parent, DetailHeaderItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Header).let {
            binding.detailPersonalHeaderTextview.text = it.title
        }
    }
}

private class PersonalDataViewHolder(
    parent: ViewGroup,
) : BaseViewHolder<DetailDataRowBinding>(parent, DetailDataRowBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Personal).let {
            binding.detailDataHeaderTextview.text = it.title
            binding.detailDataTextview.text = it.subtitle
        }
    }
}

private class CertificateViewHolder(
    parent: ViewGroup,
    private val listener: DetailClickListener,
) : BaseViewHolder<CertificateItemBinding>(parent, CertificateItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Certificate).let { cert ->
            when (cert.type) {
                VaccinationCertType.VACCINATION_COMPLETE,
                VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.info
                        } else {
                            R.color.backgroundSecondary20
                        }
                    )
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_complete)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                VaccinationCertType.VACCINATION_INCOMPLETE -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.info20
                        } else {
                            R.color.backgroundSecondary20
                        }
                    )
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_incomplete)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                TestCertType.NEGATIVE_PCR_TEST,
                TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.test_certificate_background
                        } else {
                            R.color.backgroundSecondary20
                        }
                    )
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_test)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                RecoveryCertType.RECOVERY -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.infoDark
                        } else {
                            R.color.backgroundSecondary20
                        }
                    )
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_recovery)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                TestCertType.POSITIVE_PCR_TEST,
                TestCertType.POSITIVE_ANTIGEN_TEST -> return
                // .let{} to enforce exhaustiveness
            }.let {}
            binding.certificateTypeIcon.setTint(
                if (cert.isActual &&
                    (
                        cert.certStatus == CertValidationResult.Expired ||
                            cert.certStatus == CertValidationResult.Invalid
                        )
                ) {
                    R.color.backgroundSecondary50
                } else if (cert.isActual) {
                    when (cert.type) {
                        VaccinationCertType.VACCINATION_INCOMPLETE -> R.color.info
                        else -> R.color.backgroundSecondary
                    }
                } else {
                    R.color.backgroundSecondary50
                }
            )
            binding.certificateItemActualTitle.isVisible = cert.isActual
            binding.certificateItemTitle.text = cert.title
            binding.certificateItemSubtitle.text = cert.subtitle
            binding.certificateItemDate.text = cert.date
            binding.root.setOnClickListener {
                listener.onCovCertificateClicked(cert.id, cert.type)
            }

            when (cert.certStatus) {
                CertValidationResult.Valid -> {
                    binding.certificateExpiryInfo.isGone = true
                }
                CertValidationResult.ExpiryPeriod -> {
                    binding.certificateExpiryInfo.isVisible = true
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_expires_soon_certificate_note)
                }
                CertValidationResult.Expired -> {
                    binding.certificateExpiryInfo.isVisible = true
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_expired_certificate_note)
                }
                CertValidationResult.Invalid -> {
                    binding.certificateExpiryInfo.isVisible = true
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_invalid_certificate_note)
                }
            }
        }
    }
}

private class NotificationViewHolder(
    val parent: ViewGroup,
) : BaseViewHolder<DetailNotificationItemBinding>(parent, DetailNotificationItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {

        (item as DetailItem.Notification).let {
            binding.notificationTitle.text = getString(it.titleRes)
            binding.notificationSubtitle.text = getString(it.subtitleRes)
            binding.notificationText.text = getSpanned(it.textRes, 1)
            it.iconTextRes?.let { textIcon -> binding.notificationIcon.text = getString(textIcon) }
            it.iconBackgroundRes?.let { iconRes ->
                binding.notificationIcon.background = getDrawable(parent.context, iconRes)
            }
        }
    }
}

internal abstract class BaseViewHolder<B : ViewBinding>(
    parent: ViewGroup,
    inflater: (LayoutInflater, ViewGroup, Boolean) -> B,
) : BindingViewHolder<B>(parent, inflater) {

    abstract fun onItemBind(item: DetailItem)
}

private fun ImageView.setTint(@ColorRes color: Int) {
    drawable.invalidateSelf()
    val wrapDrawable = DrawableCompat.wrap(drawable).mutate()
    DrawableCompat.setTint(
        wrapDrawable,
        ContextCompat.getColor(context, color)
    )
}

private fun View.setLayoutBackgroundColor(@ColorRes color: Int) {
    setBackgroundColor(
        ContextCompat.getColor(context, color)
    )
}
