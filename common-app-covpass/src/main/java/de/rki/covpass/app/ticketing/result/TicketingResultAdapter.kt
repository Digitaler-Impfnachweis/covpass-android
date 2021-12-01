/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing.result

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.getString
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ResultFooterBinding
import de.rki.covpass.app.databinding.ResultHeaderBinding
import de.rki.covpass.app.databinding.ResultRowBinding
import de.rki.covpass.app.detail.DisplayQrCodeFragmentNav
import de.rki.covpass.app.validityresult.InfoElementAdapter
import de.rki.covpass.app.validityresult.LocalResult
import de.rki.covpass.commonapp.uielements.showError
import de.rki.covpass.commonapp.uielements.showSuccess
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import java.util.*

@SuppressLint("NotifyDataSetChanged")
public class TicketingResultAdapter(
    parent: Fragment,
    private val resultNoteEn: Int,
    private val resultNoteDe: Int,
) : BaseRecyclerViewAdapter<BindingViewHolder<*>>(parent) {

    private var ticketingResultItems: List<TicketingResultFragment.TicketingResultRowData> = emptyList()
    private var resultType: LocalResult = LocalResult.FAIL
    private var certId: String = ""
    private lateinit var ticketingDataInitialization: TicketingDataInitialization
    private lateinit var validationServiceId: String

    public fun updateList(newTicketingResultItems: List<TicketingResultFragment.TicketingResultRowData>) {
        ticketingResultItems = newTicketingResultItems
        notifyDataSetChanged()
    }

    public fun updateHeaderWarning(
        resultType: LocalResult,
        ticketingDataInitialization: TicketingDataInitialization,
        validationServiceId: String,
    ) {
        this.resultType = resultType
        this.ticketingDataInitialization = ticketingDataInitialization
        this.validationServiceId = validationServiceId
        notifyDataSetChanged()
    }

    public fun updateCert(newCertId: String) {
        certId = newCertId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<*> =
        when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(parent)
            TYPE_NORMAL -> NormalViewHolder(parent)
            TYPE_FOOTER -> FooterViewHolder(parent, resultNoteEn, resultNoteDe)
            else -> NormalViewHolder(parent)
        }

    override fun onBindViewHolder(holder: BindingViewHolder<*>, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                (holder as? HeaderViewHolder)?.bind(resultType, ticketingDataInitialization, validationServiceId)
            }
            TYPE_NORMAL -> {
                (holder as? NormalViewHolder)?.bind(ticketingResultItems[position - 1])
            }
            TYPE_FOOTER -> {
                (holder as? FooterViewHolder)?.bind(parent, certId)
            }
        }
    }

    override fun getItemCount(): Int = ticketingResultItems.size + 2

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_HEADER
        itemCount - 1 -> TYPE_FOOTER
        else -> TYPE_NORMAL
    }

    private inner class HeaderViewHolder(parent: ViewGroup) :
        BindingViewHolder<ResultHeaderBinding>(
            parent,
            ResultHeaderBinding::inflate
        ) {
        fun bind(
            resultType: LocalResult,
            ticketingDataInitialization: TicketingDataInitialization,
            validationServiceId: String,
        ) {
            when (resultType) {
                LocalResult.FAIL -> {
                    binding.resultWarningElement.showError(
                        title = getString(R.string.share_certificate_detail_view_requirements_not_met_title),
                        subtitle = getString(
                            R.string.share_certificate_detail_view_requirements_not_met_subline,
                            validationServiceId
                        ),
                        subtitleContentDescription = getString(
                            R.string.share_certificate_detail_view_requirements_not_met_message,
                            ticketingDataInitialization.serviceProvider
                        ),
                        iconRes = R.drawable.info_error_icon
                    )
                }
                LocalResult.OPEN -> {
                    binding.resultWarningElement.showWarning(
                        title = getString(R.string.share_certificate_detail_view_requirements_not_verifiable_title),
                        subtitle = getString(
                            R.string.share_certificate_detail_view_requirements_not_verifiable_subline,
                            validationServiceId
                        ),
                        description = getString(
                            R.string.share_certificate_detail_view_requirements_not_verifiable_message,
                            ticketingDataInitialization.serviceProvider
                        ),
                        iconRes = R.drawable.info_warning_icon
                    )
                }
                else -> {
                    binding.resultWarningElement.showSuccess(
                        title = getString(R.string.share_certificate_detail_view_requirements_met_title),
                        subtitle = getString(
                            R.string.share_certificate_detail_view_requirements_met_subline,
                            validationServiceId
                        ),
                        description = getString(
                            R.string.share_certificate_detail_view_requirements_met_message,
                            ticketingDataInitialization.serviceProvider
                        ),
                        iconRes = R.drawable.info_success_icon
                    )
                }
            }
        }
    }

    private inner class NormalViewHolder(parent: ViewGroup) :
        BindingViewHolder<ResultRowBinding>(
            parent,
            ResultRowBinding::inflate
        ) {
        fun bind(item: TicketingResultFragment.TicketingResultRowData) {
            binding.resultRowHeaderTextview.text = item.title
            binding.resultRowHeaderTextview.contentDescription = item.titleAccessibleDescription
            if (item.description != null) {
                binding.resultRowSubtitleTextview.isVisible = true
                binding.resultRowSubtitleTextview.text = item.value
                binding.resultRowDataTextview.text = item.description
                if (item.valueAccessibleDescription != null) {
                    binding.resultRowSubtitleTextview.contentDescription = item.valueAccessibleDescription
                }
            } else {
                binding.resultRowSubtitleTextview.isVisible = false
                binding.resultRowDataTextview.text = item.value
                if (item.valueAccessibleDescription != null) {
                    binding.resultRowDataTextview.contentDescription = item.valueAccessibleDescription
                }
            }
            when {
                item.validationResult.find { it.result == LocalResult.FAIL } != null -> {
                    binding.resultRowDataIcon.setImageResource(R.drawable.info_error_icon)
                    binding.resultRowDataIcon.isVisible = true
                }
                item.validationResult.find { it.result == LocalResult.OPEN } != null -> {
                    binding.resultRowDataIcon.setImageResource(R.drawable.info_warning_icon)
                    binding.resultRowDataIcon.isVisible = true
                }
            }

            InfoElementAdapter(parent).attachTo(binding.infoElementList)
            (binding.infoElementList.adapter as? InfoElementAdapter)?.updateList(item.validationResult)
        }
    }

    private class FooterViewHolder(
        parent: ViewGroup,
        private val resultNoteEn: Int,
        private val resultNoteDe: Int,
    ) : BindingViewHolder<ResultFooterBinding>(
        parent,
        ResultFooterBinding::inflate
    ) {
        fun bind(parent: Fragment, certId: String) {
            binding.resultDisplayQrButton.setOnClickListener {
                parent.findNavigator().push(DisplayQrCodeFragmentNav(certId))
            }
            binding.resultInfoFooterEnglish.apply {
                text = getSpanned(resultNoteEn)
                textLocale = Locale.ENGLISH
                movementMethod = LinkMovementMethod.getInstance()
                stripUnderlines()
            }
            binding.resultInfoFooterGerman.apply {
                text = getSpanned(resultNoteDe)
                textLocale = Locale.GERMAN
                movementMethod = LinkMovementMethod.getInstance()
                stripUnderlines()
            }
            if (Locale.getDefault() == Locale.GERMANY) {
                binding.resultInfoFooterEnglish.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            } else {
                binding.resultInfoFooterGerman.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
        }
    }

    private companion object {
        const val TYPE_HEADER = 1
        const val TYPE_NORMAL = 2
        const val TYPE_FOOTER = 3
    }
}
