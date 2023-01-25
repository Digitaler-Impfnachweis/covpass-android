/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.boosterreissue

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.detail.adapter.ReissueCertificateItem
import de.rki.covpass.app.detail.adapter.ReissueCertificateViewHolder
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDate
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.isInFuture

@SuppressLint("NotifyDataSetChanged")
public class ReissueContentAdapter(
    parent: Fragment,
) : BaseRecyclerViewAdapter<ReissueCertificateViewHolder>(parent) {

    private lateinit var items: List<ReissueCertificateItem>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ReissueCertificateViewHolder {
        return ReissueCertificateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ReissueCertificateViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(items: List<ReissueCertificateItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}

public fun CombinedCovCertificate.toReissueCertificateItem(isFirst: Boolean = false): ReissueCertificateItem? {
    return when (val groupedDgcEntry = covCertificate.dgcEntry) {
        is Vaccination -> {
            ReissueCertificateItem(
                type = groupedDgcEntry.type,
                fullname = covCertificate.fullName,
                certificateType = getString(R.string.certificates_overview_vaccination_certificate_title),
                certificateProgress = getString(
                    R.string.certificates_overview_vaccination_certificate_message,
                    groupedDgcEntry.doseNumber,
                    groupedDgcEntry.totalSerialDoses,
                ),
                date = getString(
                    R.string.certificates_overview_vaccination_certificate_date,
                    groupedDgcEntry.occurrence?.formatDate() ?: "",
                ),
                certStatus = status,
                isFirst = isFirst,
            )
        }
        is Recovery -> {
            val date = if (groupedDgcEntry.validFrom.isInFuture()) {
                getString(
                    R.string.certificates_overview_recovery_certificate_valid_from_date,
                    groupedDgcEntry.validFrom?.formatDateOrEmpty() ?: "",
                )
            } else {
                getString(
                    R.string.certificates_overview_recovery_certificate_sample_date,
                    groupedDgcEntry.firstResult?.formatDateOrEmpty() ?: "",
                )
            }
            ReissueCertificateItem(
                type = groupedDgcEntry.type,
                fullname = covCertificate.fullName,
                certificateType = getString(R.string.certificates_overview_recovery_certificate_title),
                certificateProgress = getString(R.string.certificates_overview_recovery_certificate_message),
                date = date,
                certStatus = status,
                isFirst = isFirst,
            )
        }
        is TestCert -> null
    }
}
