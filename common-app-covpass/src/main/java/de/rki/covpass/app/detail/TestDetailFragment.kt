/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.countries.CountryResolver
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime

@Parcelize
internal class TestDetailFragmentNav(var certId: String) : FragmentNav(TestDetailFragment::class)

/**
 * Fragment for displaying the details of a [TestCert].
 */
internal class TestDetailFragment : DgcEntryDetailFragment() {

    override val certId: String by lazy { args.certId }

    private val args: TestDetailFragmentNav by lazy { getArgs() }

    override fun getToolbarTitleText(cert: CovCertificate): String =
        if (cert.test?.testType == TestCert.PCR_TEST) {
            getString(R.string.test_certificate_detail_view_pcr_test_title)
        } else {
            getString(R.string.test_certificate_detail_view_title)
        }

    override fun getHeaderText(): String = getString(R.string.test_certificate_detail_view_headline)

    override fun getHeaderAccessibleText(): String =
        getString(R.string.accessibility_test_certificate_detail_view_headline)

    override fun getDataRows(cert: CovCertificate): List<DataRow> {
        val test = cert.dgcEntry as? TestCert ?: return emptyList()
        return listOf(
            DataRow(
                getString(R.string.test_certificate_detail_view_data_name),
                getString(R.string.accessibility_test_certificate_detail_view_data_name),
                cert.fullNameReverse,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_name_standard),
                getString(R.string.accessibility_test_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_date_of_birth),
                getString(R.string.accessibility_test_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_disease),
                getString(R.string.accessibility_test_certificate_detail_view_data_disease),
                valueSetsRepository.getDiseaseAgentName(test.targetDisease),
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_type),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_type),
                valueSetsRepository.getTestTypeName(test.testType),
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_name),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_name),
                test.testName,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_manufactur),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_manufacturer),
                test.manufacturer?.let { valueSetsRepository.getTestManufacturerName(it) },
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_date_and_time),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_date_and_time),
                test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational(),
                valueAccessibleDescription = test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeAccessibility(),
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_results),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_results),
                valueSetsRepository.getTestResultName(test.testResult),
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_centre),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_centre),
                test.testingCenter,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_country),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_country),
                CountryResolver.getCountryLocalized(test.country),
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_issuer),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_issuer),
                test.certificateIssuer,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_identifier),
                getString(R.string.accessibility_test_certificate_detail_view_data_test_identifier),
                test.idWithoutPrefix,
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_expiry_date),
                getString(R.string.accessibility_test_certificate_detail_view_data_expiry_date),
                getString(
                    R.string.test_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime(),
                ),
                getString(R.string.test_certificate_detail_view_data_expiry_date_note),
                valueAccessibleDescription = getString(
                    R.string.test_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTimeAccessibility(),
                ),
            ),
        )
    }
}
