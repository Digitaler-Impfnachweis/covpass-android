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
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.time.ZoneOffset

@Parcelize
internal class VaccinationResultFragmentNav(
    var certId: String,
    val derivedValidationResults: List<DerivedValidationResult>,
    val country: Country,
    val dateTime: LocalDateTime,
    val rulesCount: Int,
) : FragmentNav(VaccinationResultFragment::class)

internal class VaccinationResultFragment : ResultFragment() {

    private val args: VaccinationResultFragmentNav by lazy { getArgs() }

    override val certId: String by lazy { args.certId }

    override val subtitleString: String by lazy {
        getString(R.string.certificate_check_validity_detail_view_vaccination_result_title)
    }
    override val derivedValidationResults: List<DerivedValidationResult> by lazy { args.derivedValidationResults }
    override val country: Country by lazy { args.country }
    override val dateTime: LocalDateTime by lazy { args.dateTime }
    override val rulesCount: Int by lazy { args.rulesCount }
    override val resultNoteEn: Int = R.string.certificate_check_validity_detail_view_vaccination_result_note_en
    override val resultNoteDe: Int = R.string.certificate_check_validity_detail_view_vaccination_result_note_de

    override fun getRowList(cert: CovCertificate): List<ResultRowData> {
        val vaccination = cert.dgcEntry as? Vaccination ?: return emptyList()
        return listOf(
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_name),
                cert.fullNameReverse
            ),
            ResultRowData(
                getString(R.string.test_certificate_detail_view_data_name_standard),
                cert.fullTransliteratedNameReverse
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_date_of_birth),
                cert.birthDateFormatted
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_disease),
                getDiseaseAgentName(vaccination.targetDisease),
                args.derivedValidationResults.getResultsBy("tg")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine),
                getProductName(vaccination.product),
                args.derivedValidationResults.getResultsBy("mp")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_type),
                getProphylaxisName(vaccination.vaccineCode),
                args.derivedValidationResults.getResultsBy("vp")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_manufactur),
                getManufacturerName(vaccination.manufacturer),
                args.derivedValidationResults.getResultsBy("ma")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_number),
                "${vaccination.doseNumber}/${vaccination.totalSerialDoses}",
                args.derivedValidationResults.getResultsBy("dn") +
                    args.derivedValidationResults.getResultsBy("sd")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_date_),
                vaccination.occurrence?.formatDateInternational() ?: "",
                args.derivedValidationResults.getResultsBy("dt")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_country),
                getCountryName(vaccination.country),
                args.derivedValidationResults.getResultsBy("co")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_issuer),
                vaccination.certificateIssuer,
                args.derivedValidationResults.getResultsBy("is")
            ),
            ResultRowData(
                getString(R.string.vaccination_certificate_detail_view_data_vaccine_identifier),
                vaccination.idWithoutPrefix
            ),
            ResultRowData(
                title = getString(R.string.vaccination_certificate_detail_view_data_expiry_date),
                value = getString(
                    R.string.vaccination_certificate_detail_view_data_expiry_date_message,
                    LocalDateTime.ofInstant(cert.validUntil, ZoneOffset.UTC).formatDateTime()
                ),
                description = getString(R.string.vaccination_certificate_detail_view_data_expiry_date_note)
            )
        )
    }
}
