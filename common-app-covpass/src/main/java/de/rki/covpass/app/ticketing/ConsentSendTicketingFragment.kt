/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.reactiveState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ConsentSendTicketingBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailExportPdfFragment
import de.rki.covpass.app.ticketing.result.TicketingResultScreenFragmentNav
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.ticketing.data.validate.BookingValidationResponse
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize

@Parcelize
public class ConsentSendTicketingFragmentNav(
    public val certId: String,
    public val ticketingDataInitialization: TicketingDataInitialization,
    public val validationTicketingTestObject: ValidationTicketingTestObject,
) : FragmentNav(ConsentSendTicketingFragment::class)

public class ConsentSendTicketingFragment : BaseTicketingFragment(), ValidationTicketingEvents {

    private val certs by lazy { covpassDeps.certRepository.certs.value }
    private val args: ConsentSendTicketingFragmentNav by lazy { getArgs() }
    private val binding by viewBinding(ConsentSendTicketingBinding::inflate)
    private val viewModel by reactiveState { ValidateTicketingViewModel(scope) }
    override val buttonTextRes: Int = R.string.share_certificate_transmission_action_button_agree
    override val cancelProcess: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.share_certificate_transmission_title)
        bottomSheetBinding.bottomSheetCancelButton.apply {
            isVisible = true
            setText(R.string.share_certificate_action_button_cancel)
            setOnClickListener {
                onCloseButtonClicked()
            }
        }

        updateView(
            args.ticketingDataInitialization.serviceProvider,
            args.validationTicketingTestObject.validationServiceId,
            args.ticketingDataInitialization.privacyUrl
        )

        updateCertificateView(
            certs.getCombinedCertificate(args.certId)
                ?: throw DetailExportPdfFragment.NullCertificateException()
        )
    }

    override fun onCancelTicketing(popOnce: Boolean) {
        viewModel.cancel(
            args.validationTicketingTestObject.accessTokenValidationUrl,
            args.ticketingDataInitialization.token
        )
    }

    override fun onActionButtonClicked() {
        bottomSheetBinding.bottomSheetActionButton.isEnabled = false
        binding.consentSendRecyclerView.isVisible = false
        binding.resultLoadingLayout.isVisible = true
        viewModel.validate(args.validationTicketingTestObject)
    }

    override fun onBackPressed(): Abortable {
        findNavigator().pop()
        return Abort
    }

    private fun updateView(provider: String, validationServiceId: String, privacyUrl: String) {
        val list = mutableListOf(
            ConsentInitItem.TicketingData(
                getString(R.string.share_certificate_transmission_details_booking),
                validationServiceId
            ),
            ConsentInitItem.TicketingData(
                getString(R.string.share_certificate_transmission_details_provider),
                provider
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_transmission_message)
            ),
            ConsentInitItem.Infobox(
                getString(R.string.share_certificate_transmission_consent_title),
                getString(
                    R.string.share_certificate_transmission_consent_message,
                    validationServiceId
                ),
                listOf(
                    getString(R.string.share_certificate_transmission_consent_first_list_item),
                    getString(R.string.share_certificate_transmission_consent_second_list_item, provider),
                    getString(R.string.share_certificate_transmission_consent_second_list_item_first_subitem),
                    getString(R.string.share_certificate_transmission_consent_second_list_item_second_subitem),
                    getString(R.string.share_certificate_transmission_consent_second_list_item_third_subitem),
                    getString(R.string.share_certificate_transmission_consent_second_list_item_fourth_subitem),
                )
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_transmission_notes_first_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_transmission_notes_second_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_transmission_notes_third_list_item),
                true
            ),
            ConsentInitItem.Note(
                getString(R.string.share_certificate_transmission_notes_fourth_list_item),
                true
            ),
            ConsentInitItem.DataProtection(
                getString(R.string.share_certificate_transmission_note_privacy_notice),
                R.string.share_certificate_transmission_privacy_notice_linked,
                privacyUrl
            )
        )
        ConsentTicketingAdapter(list, this).attachTo(binding.consentSendRecyclerView)
    }

    private fun updateCertificateView(combinedCovCertificate: CombinedCovCertificate) {
        with(binding.certificateFilteringItem) {
            certificateDataElementArrow.isGone = true
            certificateDataElementName.text = combinedCovCertificate.covCertificate.fullName
            when (val dgcEntry = combinedCovCertificate.covCertificate.dgcEntry) {
                is Vaccination -> {
                    if (dgcEntry.isComplete) {
                        certificateDataElementLayout.setBackgroundResource(R.color.info)
                        certificateDataElementTypeIcon.setImageResource(
                            R.drawable.main_cert_status_complete_white
                        )
                    } else {
                        certificateDataElementLayout.setBackgroundResource(R.color.info20)
                        certificateDataElementTypeIcon.setImageResource(
                            R.drawable.main_cert_status_incomplete
                        )
                    }
                    certificateDataElementType.setText(R.string.certificate_check_validity_vaccination)
                    certificateDataElementInfo.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_vaccination_certificate_message,
                        dgcEntry.doseNumber,
                        dgcEntry.totalSerialDoses
                    )
                    certificateDataElementDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_vaccination_certificate_date,
                        dgcEntry.occurrence.formatDateOrEmpty()
                    )
                }
                is TestCert -> {
                    certificateDataElementLayout.setBackgroundResource(R.color.test_certificate_background)
                    certificateDataElementType.setText(R.string.certificate_check_validity_test)
                    certificateDataElementTypeIcon.setImageResource(R.drawable.main_cert_test_white)
                    if (dgcEntry.testType == TestCert.PCR_TEST) {
                        certificateDataElementInfo.setText(R.string.test_certificate_detail_view_pcr_test_title)
                    } else {
                        certificateDataElementInfo.setText(R.string.test_certificate_detail_view_title)
                    }
                    certificateDataElementDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_test_certificate_date,
                        dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: ""
                    )
                }
                is Recovery -> {
                    certificateDataElementLayout.setBackgroundResource(R.color.info90)
                    certificateDataElementType.setText(R.string.certificate_check_validity_recovery)
                    certificateDataElementTypeIcon.setImageResource(R.drawable.main_cert_status_complete_white)
                    certificateDataElementInfo.setText(R.string.recovery_certificate_detail_view_title)
                    certificateDataElementDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_recovery_certificate_valid_until_date,
                        combinedCovCertificate.covCertificate.validUntil?.formatDateOrEmpty() ?: ""
                    )
                }
            }
        }
    }

    override fun onValidationComplete(bookingValidationResponse: BookingValidationResponse) {
        viewModel.showResult(bookingValidationResponse)
    }

    override fun onResult(bookingValidationResponse: BookingValidationResponse) {
        findNavigator().push(
            TicketingResultScreenFragmentNav(
                args.certId,
                args.ticketingDataInitialization,
                bookingValidationResponse,
                args.validationTicketingTestObject.validationServiceId
            )
        )
    }
}
