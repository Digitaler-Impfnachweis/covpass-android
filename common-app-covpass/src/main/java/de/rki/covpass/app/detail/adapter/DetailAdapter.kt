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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.*
import de.rki.covpass.app.detail.DetailClickListener
import de.rki.covpass.commonapp.utils.CertificateType

/**
 * Adapter which holds the data for Detail screen.
 * Holds all possible [CertificateType]'s
 */
public class DetailAdapter(
    private val items: List<DetailItem>,
    private val listener: DetailClickListener
) : RecyclerView.Adapter<DetailAdapter.BaseViewHolder>() {

    private companion object {
        private const val ITEM_VIEW_TYPE_NAME = 0
        private const val ITEM_VIEW_TYPE_WIDGET = 1
        private const val ITEM_VIEW_TYPE_HEADER = 2
        private const val ITEM_VIEW_TYPE_PERSONAL = 3
        private const val ITEM_VIEW_TYPE_CERTIFICATE = 4
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_VIEW_TYPE_NAME -> FullnameViewHolder(
                DetailFullnameItemBinding.inflate(inflater, parent, false)
            )
            ITEM_VIEW_TYPE_WIDGET -> WidgetViewHolder(
                DetailWidgetItemBinding.inflate(inflater, parent, false),
                listener
            )
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder(
                DetailHeaderItemBinding.inflate(inflater, parent, false)
            )
            ITEM_VIEW_TYPE_PERSONAL -> PersonalDataViewHolder(
                DetailDataRowBinding.inflate(inflater, parent, false)
            )
            ITEM_VIEW_TYPE_CERTIFICATE -> CertificateViewHolder(
                CertificateItemBinding.inflate(inflater, parent, false),
                listener
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onItemBind(items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DetailItem.Name -> ITEM_VIEW_TYPE_NAME
            is DetailItem.Widget -> ITEM_VIEW_TYPE_WIDGET
            is DetailItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DetailItem.Personal -> ITEM_VIEW_TYPE_PERSONAL
            is DetailItem.Certificate -> ITEM_VIEW_TYPE_CERTIFICATE
        }
    }

    public class FullnameViewHolder(
        private val binding: DetailFullnameItemBinding
    ) : BaseViewHolder(binding) {

        override fun onItemBind(item: DetailItem) {
            (item as DetailItem.Name).let {
                binding.detailNameTextview.text = it.fullname
            }
        }
    }

    public class WidgetViewHolder(
        private val binding: DetailWidgetItemBinding,
        private val listener: DetailClickListener
    ) : BaseViewHolder(binding) {

        override fun onItemBind(item: DetailItem) {
            (item as DetailItem.Widget).let { widget ->
                binding.detailStatusHeaderTextview.text = widget.title
                binding.detailStatusImageview.setImageResource(widget.statusIcon)
                binding.detailStatusTextview.text = widget.message
                binding.detailShowCertificateButton.text = widget.buttonText
                binding.detailShowCertificateButton.setOnClickListener {
                    listener.onShowCertificateClicked()
                }
            }
        }
    }

    public class HeaderViewHolder(
        private val binding: DetailHeaderItemBinding
    ) : BaseViewHolder(binding) {

        override fun onItemBind(item: DetailItem) {
            (item as DetailItem.Header).let {
                binding.detailPersonalHeaderTextview.text = it.title
            }
        }
    }

    public class PersonalDataViewHolder(
        private val binding: DetailDataRowBinding
    ) : BaseViewHolder(binding) {

        override fun onItemBind(item: DetailItem) {
            (item as DetailItem.Personal).let {
                binding.detailDataHeaderTextview.text = it.title
                binding.detailDataTextview.text = it.subtitle
            }
        }
    }

    public class CertificateViewHolder(
        private val binding: CertificateItemBinding,
        private val listener: DetailClickListener
    ) : BaseViewHolder(binding) {

        override fun onItemBind(item: DetailItem) {
            (item as DetailItem.Certificate).let { cert ->
                when (cert.type) {
                    CertificateType.VACCINATION_FULL_PROTECTION -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_complete)
                        binding.certificateStatusLayout.setLayoutBackgroundColor(
                            if (cert.isActual) {
                                R.color.info
                            } else {
                                R.color.backgroundSecondary20
                            }
                        )
                    }
                    CertificateType.VACCINATION_COMPLETE,
                    CertificateType.VACCINATION_INCOMPLETE -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_incomplete)
                        binding.certificateStatusLayout.setLayoutBackgroundColor(
                            if (cert.isActual) {
                                R.color.info20
                            } else {
                                R.color.backgroundSecondary20
                            }
                        )
                    }
                    CertificateType.NEGATIVE_PCR_TEST,
                    CertificateType.NEGATIVE_ANTIGEN_TEST -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_test)
                        binding.certificateStatusLayout.setLayoutBackgroundColor(
                            if (cert.isActual) {
                                R.color.test_certificate_background
                            } else {
                                R.color.backgroundSecondary20
                            }
                        )
                    }
                    CertificateType.RECOVERY -> {
                        binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_recovery)
                        binding.certificateStatusLayout.setLayoutBackgroundColor(
                            if (cert.isActual) {
                                R.color.infoDark
                            } else {
                                R.color.backgroundSecondary20
                            }
                        )
                    }
                    CertificateType.POSITIVE_PCR_TEST,
                    CertificateType.POSITIVE_ANTIGEN_TEST -> return
                }
                binding.certificateTypeIcon.setTint(
                    if (cert.isActual) {
                        R.color.backgroundSecondary
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
            }
        }
    }

    public abstract class BaseViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        public abstract fun onItemBind(item: DetailItem)
    }
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
