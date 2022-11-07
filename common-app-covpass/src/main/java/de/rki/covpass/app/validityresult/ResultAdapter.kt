package de.rki.covpass.app.validityresult

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
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.app.validitycheck.countries.CountryResolver.defaultDeDomesticCountry
import de.rki.covpass.commonapp.uielements.showError
import de.rki.covpass.commonapp.uielements.showSuccess
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import java.time.LocalDateTime
import java.util.Locale

@SuppressLint("NotifyDataSetChanged")
public class ResultAdapter(
    parent: Fragment,
    private val resultNoteEn: Int,
    private val resultNoteDe: Int,
) : BaseRecyclerViewAdapter<BindingViewHolder<*>>(parent) {

    private var resultItems: List<ResultFragment.ResultRowData> = emptyList()
    private var resultType: LocalResult = LocalResult.FAIL
    private var country: Country = defaultDeDomesticCountry
    private var dateTime: LocalDateTime = LocalDateTime.now()
    private var certId: String = ""
    private var rulesCount: Int = 0

    public fun updateList(newResultItems: List<ResultFragment.ResultRowData>) {
        resultItems = newResultItems
        notifyDataSetChanged()
    }

    public fun updateHeaderWarning(
        resultType: LocalResult,
        countryName: Country,
        dateTime: LocalDateTime,
        noRules: Int = 0,
    ) {
        this.resultType = resultType
        this.country = countryName
        this.dateTime = dateTime
        this.rulesCount = noRules
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
                (holder as? HeaderViewHolder)?.bind(resultType, country, dateTime, rulesCount)
            }
            TYPE_NORMAL -> {
                (holder as? NormalViewHolder)?.bind(resultItems[position - 1])
            }
            TYPE_FOOTER -> {
                (holder as? FooterViewHolder)?.bind(parent, certId)
            }
        }
    }

    override fun getItemCount(): Int = resultItems.size + 2

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> TYPE_HEADER
        itemCount - 1 -> TYPE_FOOTER
        else -> TYPE_NORMAL
    }

    public class HeaderViewHolder(parent: ViewGroup) :
        BindingViewHolder<ResultHeaderBinding>(
            parent,
            ResultHeaderBinding::inflate,
        ) {
        public fun bind(
            resultType: LocalResult,
            country: Country,
            dateTime: LocalDateTime,
            rulesCount: Int,
        ) {
            when (resultType) {
                LocalResult.FAIL -> {
                    binding.resultWarningElement.showError(
                        title = getString(R.string.certificate_check_validity_detail_view_result_not_valid_title),
                        subtitle = getString(
                            R.string.certificate_check_validity_detail_view_result_not_valid_message,
                            getString(country.nameRes),
                            dateTime.formatDateTime(),
                        ),
                        subtitleContentDescription = getString(
                            R.string.certificate_check_validity_detail_view_result_not_testable_first_message,
                            getString(country.nameRes),
                            dateTime.formatDateTimeAccessibility(),
                        ),
                        iconRes = R.drawable.info_error_icon,
                    )
                }
                LocalResult.OPEN -> {
                    binding.resultWarningElement.showWarning(
                        title = getString(R.string.certificate_check_validity_detail_view_result_not_testable_title),
                        subtitle = getString(
                            R.string.certificate_check_validity_detail_view_result_not_testable_first_message,
                            getString(country.nameRes),
                            dateTime.formatDateTime(),
                        ),
                        subtitleContentDescription = getString(
                            R.string.certificate_check_validity_detail_view_result_not_testable_first_message,
                            getString(country.nameRes),
                            dateTime.formatDateTimeAccessibility(),
                        ),
                        descriptionNoLink = getString(
                            R.string.certificate_check_validity_detail_view_result_not_testable_second_message,
                        ),
                        iconRes = R.drawable.info_warning_icon,
                    )
                }
                else -> {
                    if (rulesCount > 0) {
                        binding.resultWarningElement.showSuccess(
                            title = getString(R.string.certificate_check_validity_detail_view_result_valid_title),
                            subtitle = getString(
                                R.string.certificate_check_validity_detail_view_result_valid_message,
                                getString(country.nameRes),
                                dateTime.formatDateTime(),
                            ),
                            subtitleContentDescription = getString(
                                R.string.certificate_check_validity_detail_view_result_not_testable_first_message,
                                getString(country.nameRes),
                                dateTime.formatDateTimeAccessibility(),
                            ),
                            descriptionNoLink = getString(
                                R.string.certificate_check_validity_detail_view_result_valid_info,
                                rulesCount,
                            ),
                            iconRes = R.drawable.info_success_icon,
                        )
                    } else {
                        binding.resultWarningElement.showWarning(
                            title = getString(R.string.certificate_check_validity_detail_view_result_no_rules_title),
                            subtitle = getString(
                                R.string.certificate_check_validity_detail_view_result_valid_message,
                                getString(country.nameRes),
                                dateTime.formatDateTime(),
                            ),
                            subtitleContentDescription = getString(
                                R.string.certificate_check_validity_detail_view_result_not_testable_first_message,
                                getString(country.nameRes),
                                dateTime.formatDateTimeAccessibility(),
                            ),
                            descriptionNoLink = getString(
                                R.string.certificate_check_validity_detail_view_result_no_rules_message,
                            ),
                            iconRes = R.drawable.info_warning_icon,
                        )
                    }
                }
            }
        }
    }

    public inner class NormalViewHolder(parent: ViewGroup) :
        BindingViewHolder<ResultRowBinding>(
            parent,
            ResultRowBinding::inflate,
        ) {
        public fun bind(item: ResultFragment.ResultRowData) {
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

    public class FooterViewHolder(
        parent: ViewGroup,
        private val resultNoteEn: Int,
        private val resultNoteDe: Int,
    ) : BindingViewHolder<ResultFooterBinding>(
        parent,
        ResultFooterBinding::inflate,
    ) {
        public fun bind(parent: Fragment, certId: String) {
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
