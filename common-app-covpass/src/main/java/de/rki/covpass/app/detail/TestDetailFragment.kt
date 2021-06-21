/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.formatInternationalOrEmpty
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TestDetailFragmentNav(var certId: String) : FragmentNav(TestDetailFragment::class)

/**
 * Fragment for displaying the details of a [Test].
 */
internal class TestDetailFragment : DgcEntryDetailFragment() {

    override val certId: String by lazy { args.certId }

    private val args: TestDetailFragmentNav by lazy { getArgs() }

    override fun getToolbarTitleText(cert: CovCertificate): String =
        if (cert.test?.testType == Test.PCR_TEST) {
            getString(R.string.test_certificate_detail_view_pcr_test_title)
        } else {
            getString(R.string.test_certificate_detail_view_title)
        }

    override fun getHeaderText(): String = getString(R.string.test_certificate_detail_view_headline)

    override fun getDataRows(cert: CovCertificate): List<Pair<String, String>> {
        val dataRows = mutableListOf<Pair<String, String>>()
        val test = cert.dgcEntry as? Test ?: return dataRows
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_name),
            cert.fullNameReverse,
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_date_of_birth),
            cert.birthDate.formatInternationalOrEmpty(),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_disease),
            getDiseaseAgentName(test.targetDisease),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_type),
            getTestTypeName(test.testType),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_name),
            test.testName,
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_manufactur),
            getTestManufacturerName(test.manufacturer),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_date_and_time),
            test.sampleCollection?.toDeviceTimeZone()?.formatDateTimeInternational(),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_results),
            getTestResultName(test.testResult),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_centre),
            test.testingCentre,
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_country),
            getCountryName(test.country),
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_issuer),
            test.certificateIssuer,
            dataRows
        )
        addDataRow(
            getString(R.string.test_certificate_detail_view_data_test_identifier),
            test.idWithoutPrefix,
            dataRows
        )
        return dataRows
    }
}
