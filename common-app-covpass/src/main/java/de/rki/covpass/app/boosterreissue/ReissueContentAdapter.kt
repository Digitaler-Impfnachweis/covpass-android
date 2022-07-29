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
import de.rki.covpass.app.detail.adapter.CertificateViewHolder
import de.rki.covpass.app.detail.adapter.DetailItem
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.*

@SuppressLint("NotifyDataSetChanged")
public class ReissueContentAdapter(
    parent: Fragment
) : BaseRecyclerViewAdapter<CertificateViewHolder>(parent) {

    private lateinit var items: List<DetailItem>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CertificateViewHolder {
        return CertificateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) {
        holder.onItemBind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public fun updateList(items: List<DetailItem>) {
        this.items = items
        notifyDataSetChanged()
    }
}

public fun CombinedCovCertificate.toDetailItemCertificate(): DetailItem.Certificate? {
    return when (val groupedDgcEntry = covCertificate.dgcEntry) {
        is Vaccination -> {
            DetailItem.Certificate(
                id = groupedDgcEntry.id,
                type = groupedDgcEntry.type,
                title = getString(R.string.certificates_overview_vaccination_certificate_title),
                subtitle = getString(
                    R.string.certificates_overview_vaccination_certificate_message,
                    groupedDgcEntry.doseNumber, groupedDgcEntry.totalSerialDoses
                ),
                date = getString(
                    R.string.certificates_overview_vaccination_certificate_date,
                    groupedDgcEntry.occurrence?.formatDate() ?: ""
                ),
                certStatus = status,
                isActual = true
            )
        }
        is Recovery -> {
            val date = if (groupedDgcEntry.validFrom.isInFuture()) {
                getString(
                    R.string.certificates_overview_recovery_certificate_valid_from_date,
                    groupedDgcEntry.validFrom?.formatDateOrEmpty() ?: ""
                )
            } else {
                getString(
                    R.string.certificates_overview_recovery_certificate_valid_until_date,
                    groupedDgcEntry.validUntil?.formatDateOrEmpty() ?: ""
                )
            }
            DetailItem.Certificate(
                id = groupedDgcEntry.id,
                type = groupedDgcEntry.type,
                title = getString(R.string.certificates_overview_recovery_certificate_title),
                subtitle = getString(R.string.certificates_overview_recovery_certificate_message),
                date = date,
                certStatus = status,
                isActual = true
            )
        }
        is TestCert -> null
    }
}
