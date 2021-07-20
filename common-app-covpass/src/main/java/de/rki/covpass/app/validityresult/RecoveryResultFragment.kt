/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validityresult

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.sdk.cert.getCountryName
import de.rki.covpass.sdk.cert.getDiseaseAgentName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatInternationalOrEmpty
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
internal class RecoveryResultFragmentNav(
    var certId: String,
    val derivedValidationResults: List<DerivedValidationResult>,
    val country: Country,
    val dateTime: LocalDateTime,
    val rulesCount: Int
) : FragmentNav(RecoveryResultFragment::class)

internal class RecoveryResultFragment : ResultFragment() {

    private val args: RecoveryResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_recovery_result_title)
    }
    override val derivedValidationResults: List<DerivedValidationResult> by lazy { args.derivedValidationResults }
    override val country: Country by lazy { args.country }
    override val dateTime: LocalDateTime by lazy { args.dateTime }
    override val rulesCount: Int by lazy { args.rulesCount }

    override fun getRowList(cert: CovCertificate): List<ResultRowData> {
        val recovery = cert.dgcEntry as? Recovery ?: return emptyList()
        return listOf(
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_date_of_birth),
                cert.birthDate.formatInternationalOrEmpty()
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_disease),
                getDiseaseAgentName(recovery.targetDisease),
                args.derivedValidationResults.filter { it.affectedString == "tg" }
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_date_first_positive_result),
                recovery.firstResult?.formatDateInternational() ?: "",
                args.derivedValidationResults.filter { it.affectedString == "fr" }

            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_country),
                getCountryName(recovery.country),
                args.derivedValidationResults.filter { it.affectedString == "co" }
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_issuer),
                recovery.certificateIssuer,
                args.derivedValidationResults.filter { it.affectedString == "is" }
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_valid_from),
                recovery.validFrom?.formatDateInternational() ?: "",
                args.derivedValidationResults.filter { it.affectedString == "df" }
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_valid_until),
                recovery.validUntil?.formatDateInternational() ?: "",
                args.derivedValidationResults.filter { it.affectedString == "du" }
            ),
            ResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_identifier),
                recovery.idWithoutPrefix
            )
        )
    }
}
