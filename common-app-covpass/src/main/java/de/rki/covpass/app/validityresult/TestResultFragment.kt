/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.validityresult

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.validitycheck.countries.Country
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.ZoneOffset

@Parcelize
internal class TestResultFragmentNav(
    val certId: String,
    val derivedValidationResults: List<DerivedValidationResult>,
    val countryName: Country,
    val dateTime: LocalDateTime,
    val rulesCount: Int
) : FragmentNav(TestResultFragment::class)

internal class TestResultFragment : ResultFragment() {

    private val args: TestResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_test_result_title)
    }
    override val derivedValidationResults: List<DerivedValidationResult> by lazy { args.derivedValidationResults }
    override val country: Country by lazy { args.countryName }
    override val dateTime: LocalDateTime by lazy { args.dateTime }
    override val rulesCount: Int by lazy { args.rulesCount }
    override val resultNoteEn: Int = R.string.certificate_check_validity_detail_view_test_result_note_en
    override val resultNoteDe: Int = R.string.certificate_check_validity_detail_view_test_result_note_de

    override fun getRowList(cert: CovCertificate): List<ResultRowData> {
        val test = cert.dgcEntry as? Test ?: return emptyList()
        return listOf(
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_disease),
                getDiseaseAgentName(test.targetDisease),
                args.derivedValidationResults.getResultsBy("tg")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_type),
                getTestTypeName(test.testType),
                args.derivedValidationResults.getResultsBy("tt")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_name),
                test.testName,
                args.derivedValidationResults.getResultsBy("nm")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_manufactur),
                getTestManufacturerName(test.manufacturer),
                args.derivedValidationResults.getResultsBy("ma")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_date_and_time),
                test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational() ?: "",
                args.derivedValidationResults.getResultsBy("sc")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_results),
                getTestResultName(test.testResult),
                args.derivedValidationResults.getResultsBy("tr")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_centre),
                test.testingCentre,
                args.derivedValidationResults.getResultsBy("tc")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_country),
                getCountryName(test.country),
                args.derivedValidationResults.getResultsBy("co")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_issuer),
                test.certificateIssuer,
                args.derivedValidationResults.getResultsBy("is")
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_identifier),
                test.idWithoutPrefix
            ),
            ResultRowData(
                title = getString(R.string.test_certificate_detail_view_data_expiry_date),
                value = getString(
                    R.string.text_certificate_detail_view_data_expiry_date_message,
                    LocalDateTime.ofInstant(cert.validUntil, ZoneOffset.UTC).formatDateTime()
                ),
                description = getString(R.string.test_certificate_detail_view_data_expiry_date_note)
            )
        )
    }
}
