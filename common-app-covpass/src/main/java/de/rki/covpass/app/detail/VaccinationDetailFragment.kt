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
import de.rki.covpass.sdk.cert.getManufacturerName
import de.rki.covpass.sdk.cert.getProductName
import de.rki.covpass.sdk.cert.getProphylaxisName
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatDateTimeAccessibility
import kotlinx.parcelize.Parcelize
import java.time.ZoneId
import java.time.ZonedDateTime

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

    override fun getHeaderAccessibleText(): String =
        getString(R.string.accessibility_vaccination_certificate_detail_view_booster_vaccination_headline)

    override fun isHeaderTitleVisible(cert: CovCertificate): Boolean {
        val vaccination = cert.dgcEntry as? Vaccination ?: return false
        return vaccination.isCompleteSingleDose
    }

    override fun getDataRows(cert: CovCertificate): List<DataRow> {
        val vaccination = cert.dgcEntry as? Vaccination ?: return emptyList()
        return listOf(
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_name),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_name_standard),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_date_of_birth),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_disease),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_disease),
                getDiseaseAgentName(vaccination.targetDisease)
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine),
                getProductName(vaccination.product)
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_type),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_type),
                getProphylaxisName(vaccination.vaccineCode)
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_manufactur),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_manufacturer),
                getManufacturerName(vaccination.manufacturer)
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_number),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_number),
                "${vaccination.doseNumber}/${vaccination.totalSerialDoses}",
                valueAccessibleDescription = getString(
                    R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_number_readable_text,
                    vaccination.doseNumber,
                    vaccination.totalSerialDoses
                )
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_date_),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_date_),
                vaccination.occurrence?.formatDateInternational(),
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_country),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_country),
                CountryRepository.getCountryLocalized(vaccination.country)
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_issuer),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_issuer),
                vaccination.certificateIssuer
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_identifier),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_vaccine_identifier),
                vaccination.idWithoutPrefix
            ),
            DataRow(
                getString(R.string.vaccination_certificate_detail_view_data_expiry_date),
                getString(R.string.accessibility_vaccination_certificate_detail_view_data_expiry_date),
                getString(
                    R.string.vaccination_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTime()
                ),
                getString(R.string.vaccination_certificate_detail_view_data_expiry_date_note),
                valueAccessibleDescription = getString(
                    R.string.vaccination_certificate_detail_view_data_expiry_date_message,
                    ZonedDateTime.ofInstant(cert.validUntil, ZoneId.systemDefault()).formatDateTimeAccessibility()
                ),
            )
        )
    }
}
