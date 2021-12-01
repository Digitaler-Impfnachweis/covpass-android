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
import de.rki.covpass.sdk.cert.getTestManufacturerName
import de.rki.covpass.sdk.cert.getTestResultName
import de.rki.covpass.sdk.cert.getTestTypeName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
internal class TicketingTestResultFragmentNav(
    val certId: String,
    val ticketingDataInitialization: TicketingDataInitialization,
    val bookingValidationResponse: BookingValidationResponse,
    val validationServiceId: String,
) : FragmentNav(TicketingTestResultFragment::class)

internal class TicketingTestResultFragment : TicketingResultFragment() {

    private val args: TicketingTestResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_test_result_title)
    }
    override val subtitleAccessibleDescription: String by lazy {
        getString(R.string.accessibility_certificate_check_validity_detail_view_test_result_title)
    }
    override val bookingValidationResponse: BookingValidationResponse by lazy { args.bookingValidationResponse }
    override val ticketingDataInitialization: TicketingDataInitialization by lazy { args.ticketingDataInitialization }
    override val resultNoteEn: Int = R.string.certificate_check_validity_detail_view_test_result_note_en
    override val resultNoteDe: Int = R.string.certificate_check_validity_detail_view_test_result_note_de
    override val validationServiceId: String by lazy { args.validationServiceId }

    override fun getRowList(cert: CovCertificate): List<TicketingResultRowData> {
        val test = cert.dgcEntry as? TestCert ?: return emptyList()
        return listOf(
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_name),
                getString(R.string.accessibility_test_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_name_standard),
                getString(R.string.accessibility_test_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_date_of_birth),
                getString(R.string.accessibility_test_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_disease),
                getString(R.string.accessibility_test_certificate_detail_view_data_disease),
                getDiseaseAgentName(test.targetDisease),
                derivedValidationResults.getResultsBy("tg")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_type),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_type),
                getTestTypeName(test.testType),
                derivedValidationResults.getResultsBy("tt")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_name),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_name),
                test.testName,
                derivedValidationResults.getResultsBy("nm")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_manufactur),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_manufacturer),
                test.manufacturer?.let { getTestManufacturerName(it) },
                derivedValidationResults.getResultsBy("ma")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_date_and_time),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_date_and_time),
                test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational() ?: "",
                derivedValidationResults.getResultsBy("sc"),
                valueAccessibleDescription = test.sampleCollection?.toDeviceTimeZone()
                    ?.formatDateTimeAccessibility() ?: ""
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_results),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_results),
                getTestResultName(test.testResult),
                derivedValidationResults.getResultsBy("tr")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_centre),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_centre),
                test.testingCenter,
                derivedValidationResults.getResultsBy("tc")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_country),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_country),
                CountryResolver.getCountryLocalized(test.country),
                derivedValidationResults.getResultsBy("co")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_issuer),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_issuer),
                test.certificateIssuer,
                derivedValidationResults.getResultsBy("is")
            ),
            TicketingResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_identifier),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_identifier),
                test.idWithoutPrefix
            ),
            TicketingResultRowData(
                title = getString(R.string.test_certificate_detail_view_data_expiry_date),
                getString(R.string.accessibility_test_certificate_detail_view_data_expiry_date),
                value = getString(
                    R.string.test_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime()
                ),
                description = getString(R.string.test_certificate_detail_view_data_expiry_date_note),
                valueAccessibleDescription = getString(
                    R.string.test_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTimeAccessibility()
                ),
            )
        )
    }
}
