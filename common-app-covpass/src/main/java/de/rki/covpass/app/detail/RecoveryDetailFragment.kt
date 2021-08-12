/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.sdk.cert.getCountryName
import de.rki.covpass.sdk.cert.getDiseaseAgentName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.ZoneOffset

@Parcelize
internal class RecoveryDetailFragmentNav(var certId: String) : FragmentNav(RecoveryDetailFragment::class)

/**
 * Fragment for displaying the details of a [Recovery].
 */
internal class RecoveryDetailFragment : DgcEntryDetailFragment() {

    override val certId: String by lazy { args.certId }

    private val args: RecoveryDetailFragmentNav by lazy { getArgs() }

    override fun getToolbarTitleText(cert: CovCertificate): String =
        getString(R.string.recovery_certificate_detail_view_title)

    override fun getHeaderText(): String = getString(R.string.recovery_certificate_detail_view_headline)

    override fun getDataRows(cert: CovCertificate): List<DataRow> {
        val recovery = cert.dgcEntry as? Recovery ?: return emptyList()
        return listOf(
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_disease),
                getDiseaseAgentName(recovery.targetDisease)
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_date_first_positive_result),
                recovery.firstResult?.formatDateInternational()
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_country),
                getCountryName(recovery.country)
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_issuer),
                recovery.certificateIssuer
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_valid_from),
                recovery.validFrom?.formatDateInternational()
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_valid_until),
                recovery.validUntil?.formatDateInternational()
            ),
            DataRow(
                getString(R.string.recovery_certificate_detail_view_data_identifier),
                recovery.idWithoutPrefix
            )
        )
    }

    override fun getExtendedDataRows(
        cert: CovCertificate
    ): List<ExtendedDataRow> = listOf(
        ExtendedDataRow(
            getString(R.string.recovery_certificate_detail_view_data_expiry_date),
            getString(
                R.string.recovery_certificate_detail_view_data_expiry_date_message,
                LocalDateTime.ofInstant(cert.validUntil, ZoneOffset.UTC).formatDateTime()
            ),
            getString(R.string.recovery_certificate_detail_view_data_expiry_date_note)
        )
    )
}
