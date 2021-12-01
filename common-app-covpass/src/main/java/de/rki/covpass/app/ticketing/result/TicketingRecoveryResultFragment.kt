/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing.result

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.validitycheck.countries.CountryResolver
import de.rki.covpass.app.validityresult.getResultsBy
import de.rki.covpass.sdk.cert.getDiseaseAgentName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
internal class TicketingRecoveryResultFragmentNav(
    var certId: String,
    val ticketingDataInitialization: TicketingDataInitialization,
    val bookingValidationResponse: BookingValidationResponse,
    val validationServiceId: String,
) : FragmentNav(TicketingRecoveryResultFragment::class)

internal class TicketingRecoveryResultFragment : TicketingResultFragment() {

    private val args: TicketingRecoveryResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_recovery_result_title)
    }
    override val subtitleAccessibleDescription: String by lazy {
        getString(R.string.accessibility_certificate_check_validity_detail_view_recovery_result_title)
    }
    override val bookingValidationResponse: BookingValidationResponse by lazy { args.bookingValidationResponse }
    override val ticketingDataInitialization: TicketingDataInitialization by lazy { args.ticketingDataInitialization }
    override val resultNoteEn: Int = R.string.certificate_check_validity_detail_view_recovery_result_note_en
    override val resultNoteDe: Int = R.string.certificate_check_validity_detail_view_recovery_result_note_de
    override val validationServiceId: String by lazy { args.validationServiceId }

    override fun getRowList(cert: CovCertificate): List<TicketingResultRowData> {
        val recovery = cert.dgcEntry as? Recovery ?: return emptyList()
        return listOf(
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_name),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_name_standard),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_date_of_birth),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_disease),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_disease),
                getDiseaseAgentName(recovery.targetDisease),
                derivedValidationResults.getResultsBy("tg")
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_date_first_positive_result),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_date_first_positive_result),
                recovery.firstResult?.formatDateInternational() ?: "",
                derivedValidationResults.getResultsBy("fr")

            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_country),
                CountryResolver.getCountryLocalized(recovery.country),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_country),
                derivedValidationResults.getResultsBy("co")
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_issuer),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_issuer),
                recovery.certificateIssuer,
                derivedValidationResults.getResultsBy("is")
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_valid_from),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_valid_from),
                recovery.validFrom?.formatDateInternational() ?: "",
                derivedValidationResults.getResultsBy("df")
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_valid_until),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_valid_until),
                recovery.validUntil?.formatDateInternational() ?: "",
                derivedValidationResults.getResultsBy("du")
            ),
            TicketingResultRowData(
                getString(R.string.recovery_certificate_detail_view_data_identifier),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_identifier),
                recovery.idWithoutPrefix
            ),
            TicketingResultRowData(
                title = getString(R.string.recovery_certificate_detail_view_data_expiry_date),
                getString(R.string.accessibility_recovery_certificate_detail_view_data_expiry_date),
                value = getString(
                    R.string.recovery_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime()
                ),
                description = getString(R.string.recovery_certificate_detail_view_data_expiry_date_note),
                valueAccessibleDescription = getString(
                    R.string.recovery_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTimeAccessibility()
                )
            )
        )
    }
}
