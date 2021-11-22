/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateFilteringItemBinding
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone

@SuppressLint("NotifyDataSetChanged")
public class CertificateFilteringTicketingAdapter(parent: Fragment) :
    BaseRecyclerViewAdapter<CertificateFilteringTicketingAdapter.CertificateFilteringViewHolder>(parent) {

    private lateinit var items: List<CombinedCovCertificate>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateFilteringViewHolder {
        return CertificateFilteringViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CertificateFilteringViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(items: List<CombinedCovCertificate>) {
        this.items = items
        notifyDataSetChanged()
    }

    public inner class CertificateFilteringViewHolder(parent: ViewGroup) :
        BindingViewHolder<CertificateFilteringItemBinding>(
            parent,
            CertificateFilteringItemBinding::inflate
        ) {
        public fun bind(item: CombinedCovCertificate) {
            with(binding) {
                binding.certLayout.setOnClickListener {
                    // TODO send to the right view
                }

                certificateFilteringItemName.text = item.covCertificate.fullName
                when (val dgcEntry = item.covCertificate.dgcEntry) {
                    is Vaccination -> {
                        if (dgcEntry.isComplete) {
                            certificateFilteringItemLayout.setBackgroundResource(R.color.info)
                            certificateFilteringItemTypeIcon.setImageResource(
                                R.drawable.main_cert_status_complete_white
                            )
                        } else {
                            certificateFilteringItemLayout.setBackgroundResource(R.color.info20)
                            certificateFilteringItemTypeIcon.setImageResource(
                                R.drawable.main_cert_status_incomplete
                            )
                        }
                        certificateFilteringItemType.setText(R.string.certificate_check_validity_vaccination)
                        certificateFilteringItemInfo.text = getString(
                            R.string.certificates_overview_vaccination_certificate_message,
                            dgcEntry.doseNumber,
                            dgcEntry.totalSerialDoses
                        )
                        certificateFilteringItemDate.text = getString(
                            R.string.certificates_overview_vaccination_certificate_date,
                            dgcEntry.validDate.formatDateOrEmpty()
                        )
                    }
                    is TestCert -> {
                        certificateFilteringItemLayout.setBackgroundResource(R.color.test_certificate_background)
                        certificateFilteringItemType.setText(R.string.certificate_check_validity_test)
                        certificateFilteringItemTypeIcon.setImageResource(R.drawable.main_cert_test_white)
                        if (dgcEntry.testType == PCR_TEST) {
                            certificateFilteringItemInfo.setText(R.string.test_certificate_detail_view_pcr_test_title)
                        } else {
                            certificateFilteringItemInfo.setText(R.string.test_certificate_detail_view_title)
                        }
                        certificateFilteringItemDate.text = getString(
                            R.string.certificates_overview_test_certificate_date,
                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: ""
                        )
                    }
                    is Recovery -> {
                        certificateFilteringItemLayout.setBackgroundResource(R.color.info90)
                        certificateFilteringItemType.setText(R.string.certificate_check_validity_recovery)
                        certificateFilteringItemTypeIcon.setImageResource(R.drawable.main_cert_status_complete_white)
                        certificateFilteringItemInfo.setText(R.string.recovery_certificate_detail_view_title)
                        certificateFilteringItemDate.text = getString(
                            R.string.certificates_overview_recovery_certificate_valid_until_date,
                            item.covCertificate.validUntil?.formatDateOrEmpty() ?: ""
                        )
                    }
                }
            }
        }
    }
}
