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
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatInternationalOrEmpty
import kotlinx.parcelize.Parcelize

@Parcelize
internal class VaccinationDetailFragmentNav(var certId: String) : FragmentNav(VaccinationDetailFragment::class)

/**
 * Fragment for displaying the details of a [Vaccination].
 */
internal class VaccinationDetailFragment : DgcEntryDetailFragment() {

    override val certId: String by lazy { args.certId }

    private val args: VaccinationDetailFragmentNav by lazy { getArgs() }

    override fun getToolbarTitleText(cert: CovCertificate): String =
        getString(
            R.string.vaccination_certificate_detail_view_vaccination_title,
            cert.vaccination?.doseNumber,
            cert.vaccination?.totalSerialDoses
        )

    override fun getHeaderText(): String = getString(R.string.vaccination_certificate_detail_view_vaccination_headline)

    override fun getDataRows(cert: CovCertificate): List<Pair<String, String>> {
        val dataRows = mutableListOf<Pair<String, String>>()
        val vaccination = cert.dgcEntry as? Vaccination ?: return dataRows
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_name),
            cert.fullNameReverse,
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_date_of_birth),
            cert.birthDate.formatInternationalOrEmpty(),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_disease),
            getDiseaseAgentName(vaccination.targetDisease),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine),
            getProductName(vaccination.product),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_type),
            getProphylaxisName(vaccination.vaccineCode),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_manufactur),
            getManufacturerName(vaccination.manufacturer),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_number),
            "${vaccination.doseNumber}/${vaccination.totalSerialDoses}",
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_date_),
            vaccination.occurrence?.formatDateInternational(),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_country),
            getCountryName(vaccination.country),
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_issuer),
            vaccination.certificateIssuer,
            dataRows
        )
        addDataRow(
            getString(R.string.vaccination_certificate_detail_view_data_vaccine_identifier),
            vaccination.idWithoutPrefix,
            dataRows
        )
        return dataRows
    }
}
