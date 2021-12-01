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
import de.rki.covpass.sdk.cert.getManufacturerName
import de.rki.covpass.sdk.cert.getProductName
import de.rki.covpass.sdk.cert.getProphylaxisName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
internal class TicketingVaccinationResultFragmentNav(
    var certId: String,
    val ticketingDataInitialization: TicketingDataInitialization,
    val bookingValidationResponse: BookingValidationResponse,
    val validationServiceId: String,
) : FragmentNav(TicketingVaccinationResultFragment::class)

internal class TicketingVaccinationResultFragment : TicketingResultFragment() {

    private val args: TicketingVaccinationResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_vaccination_result_title)
    }
    override val subtitleAccessibleDescription: String by lazy {
        getString(R.string.accessbility_certificate_check_validity_detail_view_vaccination_result_title)
    }
    override val bookingValidationResponse: BookingValidationResponse by lazy { args.bookingValidationResponse }
    override val ticketingDataInitialization: TicketingDataInitialization by lazy { args.ticketingDataInitialization }
    override val resultNoteEn: Int = R.string.certificate_check_validity_detail_view_vaccination_result_note_en
    override val resultNoteDe: Int = R.string.certificate_check_validity_detail_view_vaccination_result_note_de
    override val validationServiceId: String by lazy { args.validationServiceId }

    override fun getRowList(cert: CovCertificate): List<TicketingResultRowData> {
        val vaccination = cert.dgcEntry as? Vaccination ?: return emptyList()
        return listOf(
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_name),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_name_standard),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_date_of_birth),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_disease),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_disease),
                getDiseaseAgentName(vaccination.targetDisease),
                derivedValidationResults.getResultsBy("tg")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine),
                getProductName(vaccination.product),
                derivedValidationResults.getResultsBy("mp")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_type),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_type),
                getProphylaxisName(vaccination.vaccineCode),
                derivedValidationResults.getResultsBy("vp")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_manufactur),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_manufacturer),
                getManufacturerName(vaccination.manufacturer),
                derivedValidationResults.getResultsBy("ma")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_number),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_number),
                "${vaccination.doseNumber}/${vaccination.totalSerialDoses}",
                if (derivedValidationResults.getResultsBy("dn").isNotEmpty()) {
                    derivedValidationResults.getResultsBy("dn")
                } else {
                    derivedValidationResults.getResultsBy("sd")
                },
                valueAccessibleDescription = getString(
                    R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_number_readable_text,
                    vaccination.doseNumber,
                    vaccination.totalSerialDoses
                )
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_date_),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_date_),
                vaccination.occurrence?.formatDateInternational() ?: "",
                derivedValidationResults.getResultsBy("dt")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_country),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_country),
                CountryResolver.getCountryLocalized(vaccination.country),
                derivedValidationResults.getResultsBy("co")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_issuer),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_issuer),
                vaccination.certificateIssuer,
                derivedValidationResults.getResultsBy("is")
            ),
            TicketingResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_identifier),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_identifier),
                vaccination.idWithoutPrefix
            ),
            TicketingResultRowData(
                title = getString(R.string.vaccination_certificate_detail_view_data_expiry_date),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_expiry_date),
                value = getString(
                    R.string.vaccination_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime()
                ),
                description = getString(R.string.vaccination_certificate_detail_view_data_expiry_date_note),
                valueAccessibleDescription = getString(
                    R.string.vaccination_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTimeAccessibility()
                ),
            )
        )
    }
}
