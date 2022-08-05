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
import de.rki.covpass.app.databinding.CertificateDataElementBinding
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCert.Companion.PCR_TEST
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.ticketing.BookingPortalEncryptionData
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone

@SuppressLint("NotifyDataSetChanged")
public class CertificateFilteringTicketingAdapter(
    parent: Fragment,
    public val listener: FilterClickListener,
) : BaseRecyclerViewAdapter<CertificateFilteringTicketingAdapter.CertificateFilteringViewHolder>(parent) {

    private lateinit var items: List<CombinedCovCertificate>
    private lateinit var encryptionData: BookingPortalEncryptionData

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateFilteringViewHolder {
        return CertificateFilteringViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CertificateFilteringViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(
        items: List<CombinedCovCertificate>,
        encryptionData: BookingPortalEncryptionData,
    ) {
        this.items = items
        this.encryptionData = encryptionData
        notifyDataSetChanged()
    }

    public inner class CertificateFilteringViewHolder(parent: ViewGroup) :
        BindingViewHolder<CertificateDataElementBinding>(
            parent,
            CertificateDataElementBinding::inflate,
        ) {
        public fun bind(item: CombinedCovCertificate) {
            with(binding) {
                binding.certLayout.setOnClickListener {
                    listener.onCovCertificateClicked(
                        item.covCertificate.dgcEntry.id,
                        item.qrContent,
                        encryptionData,
                    )
                }

                certificateDataElementName.text = item.covCertificate.fullName
                when (val dgcEntry = item.covCertificate.dgcEntry) {
                    is Vaccination -> {
                        if (dgcEntry.isComplete) {
                            certificateDataElementLayout.setBackgroundResource(R.color.info)
                            certificateDataElementTypeIcon.setImageResource(
                                R.drawable.main_cert_status_complete_white,
                            )
                        } else {
                            certificateDataElementLayout.setBackgroundResource(R.color.info20)
                            certificateDataElementTypeIcon.setImageResource(
                                R.drawable.main_cert_status_incomplete,
                            )
                        }
                        certificateDataElementType.setText(R.string.certificate_check_validity_vaccination)
                        certificateDataElementInfo.text = getString(
                            R.string.certificates_overview_vaccination_certificate_message,
                            dgcEntry.doseNumber,
                            dgcEntry.totalSerialDoses,
                        )
                        certificateDataElementDate.text = getString(
                            R.string.certificates_overview_vaccination_certificate_date,
                            dgcEntry.occurrence.formatDateOrEmpty(),
                        )
                    }
                    is TestCert -> {
                        certificateDataElementLayout.setBackgroundResource(R.color.test_certificate_background)
                        certificateDataElementType.setText(R.string.certificate_check_validity_test)
                        certificateDataElementTypeIcon.setImageResource(R.drawable.main_cert_test_white)
                        if (dgcEntry.testType == PCR_TEST) {
                            certificateDataElementInfo.setText(R.string.test_certificate_detail_view_pcr_test_title)
                        } else {
                            certificateDataElementInfo.setText(R.string.test_certificate_detail_view_title)
                        }
                        certificateDataElementDate.text = getString(
                            R.string.certificates_overview_test_certificate_date,
                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: "",
                        )
                    }
                    is Recovery -> {
                        certificateDataElementLayout.setBackgroundResource(R.color.info90)
                        certificateDataElementType.setText(R.string.certificate_check_validity_recovery)
                        certificateDataElementTypeIcon.setImageResource(R.drawable.main_cert_status_complete_white)
                        certificateDataElementInfo.setText(R.string.recovery_certificate_detail_view_title)
                        certificateDataElementDate.text = getString(
                            R.string.certificates_overview_recovery_certificate_valid_until_date,
                            item.covCertificate.validUntil?.formatDateOrEmpty() ?: "",
                        )
                    }
                }
            }
        }
    }
}
