/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.validitycheck.countries.CountryRepository
import de.rki.covpass.sdk.cert.getDiseaseAgentName
import de.rki.covpass.sdk.cert.getTestManufacturerName
import de.rki.covpass.sdk.cert.getTestResultName
import de.rki.covpass.sdk.cert.getTestTypeName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.utils.formatDateTime
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

    override fun getDataRows(cert: CovCertificate): List<DataRow> {
        val test = cert.dgcEntry as? TestCert ?: return emptyList()
        return listOf(
            DataRow(
                getString(R.string.test_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_disease),
                getDiseaseAgentName(test.targetDisease)
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_type),
                getTestTypeName(test.testType)
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_name),
                test.testName
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_manufactur),
                test.manufacturer?.let { getTestManufacturerName(it) }
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_date_and_time),
                test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational()
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_results),
                getTestResultName(test.testResult)
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_centre),
                test.testingCenter
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_country),
                CountryRepository.getCountryLocalized(test.country)
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_issuer),
                test.certificateIssuer
            ),
            DataRow(
                getString(R.string.test_certificate_detail_view_data_test_identifier),
                test.idWithoutPrefix
            )
        )
    }

    override fun getExtendedDataRows(
        cert: CovCertificate
    ): List<ExtendedDataRow> = listOf(
        ExtendedDataRow(
            getString(R.string.test_certificate_detail_view_data_expiry_date),
            getString(
                R.string.test_certificate_detail_view_data_expiry_date_message,
                ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime()
            ),
            getString(R.string.test_certificate_detail_view_data_expiry_date_note)
        )
    )
}
