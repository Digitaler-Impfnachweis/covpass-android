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
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.formatInternationalOrEmpty
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

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

    override fun getRowList(cert: CovCertificate): List<ResultRowData> {
        val test = cert.dgcEntry as? Test ?: return emptyList()
        return listOf(
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_date_of_birth),
                cert.birthDate.formatInternationalOrEmpty()
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_disease),
                getDiseaseAgentName(test.targetDisease),
                args.derivedValidationResults.filter { it.affectedString == "tg" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_type),
                getTestTypeName(test.testType),
                args.derivedValidationResults.filter { it.affectedString == "tt" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_name),
                test.testName,
                args.derivedValidationResults.filter { it.affectedString == "nm" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_manufactur),
                getTestManufacturerName(test.manufacturer),
                args.derivedValidationResults.filter { it.affectedString == "ma" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_date_and_time),
                test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational() ?: "",
                args.derivedValidationResults.filter { it.affectedString == "sc" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_results),
                getTestResultName(test.testResult),
                args.derivedValidationResults.filter { it.affectedString == "tr" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_centre),
                test.testingCentre,
                args.derivedValidationResults.filter { it.affectedString == "tc" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_country),
                getCountryName(test.country),
                args.derivedValidationResults.filter { it.affectedString == "co" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_issuer),
                test.certificateIssuer,
                args.derivedValidationResults.filter { it.affectedString == "is" }
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_test_identifier),
                test.idWithoutPrefix
            )
        )
    }
}
