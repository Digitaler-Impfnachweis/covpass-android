/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.ticketing

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ConsentSendTicketingBinding
import de.rki.covpass.commonapp.BaseBottomSheet
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize

// TODO add dcc parameter
@Parcelize
public class ConsentSendTicketingFragmentNav(
    public val ticketingDataInitialization: TicketingDataInitialization,
) : FragmentNav(ConsentSendTicketingFragment::class)

public class ConsentSendTicketingFragment : BaseBottomSheet(), DialogListener {

    private val args: ConsentSendTicketingFragmentNav by lazy { getArgs() }
    private val binding by viewBinding(ConsentSendTicketingBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBinding.bottomSheetTitle.setText(R.string.share_certificate_transmission_title)
        bottomSheetBinding.bottomSheetCancelButton.isVisible = true

        updateView(
            args.ticketingDataInitialization.serviceProvider,
            args.ticketingDataInitialization.subject,
            args.ticketingDataInitialization.privacyUrl
        )
        // TODO updateCertificateView
//        updateCertificateView(
//            combinedCovCertificate
//        )
    }

    override fun onActionButtonClicked() {
        // TODO
    }

    override fun onCloseButtonClicked() {
        val dialogModel = DialogModel(
            titleRes = R.string.cancellation_share_certificate_title,
            positiveButtonTextRes = R.string.cancellation_share_certificate_action_button_yes,
            negativeButtonTextRes = R.string.cancellation_share_certificate_action_button_no,
            tag = CANCEL_TICKETING_SECOND_CONSENT,
        )
        showDialog(dialogModel, childFragmentManager)
    }

    override fun onClickOutside() {
        onCloseButtonClicked()
    }

    private fun updateView(provider: String, booking: String, privacyUrl: String) {
        val list = mutableListOf(
            // TODO fix this element
            ConsentInitItem.TicketingData(
                getString(R.string.share_certificate_transmission_details_booking),
                booking
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
                    // TODO verify value
                    args.ticketingDataInitialization.serviceProvider
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

    // TODO remove suppress
    @Suppress("UnusedPrivateMember")
    private fun updateCertificateView(combinedCovCertificate: CombinedCovCertificate) {
        with(binding.certificateFilteringItem) {
            certificateFilteringItemName.text = combinedCovCertificate.covCertificate.fullName
            when (val dgcEntry = combinedCovCertificate.covCertificate.dgcEntry) {
                is Vaccination -> {
                    if (dgcEntry.isComplete) {
                        certificateFilteringItemLayout.setBackgroundResource(R.color.info)
                        certificateFilteringItemTypeIcon.setImageResource(
                            R.drawable.main_cert_status_complete_white
                        )
                    } else {
                        certificateFilteringItemLayout.setBackgroundResource(R.color.info20)
                        certificateFilteringItemTypeIcon.setImageResource(
                            R.drawable.main_cert_status_incomplete
                        )
                    }
                    certificateFilteringItemType.setText(R.string.certificate_check_validity_vaccination)
                    certificateFilteringItemInfo.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_vaccination_certificate_message,
                        dgcEntry.doseNumber,
                        dgcEntry.totalSerialDoses
                    )
                    certificateFilteringItemDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_vaccination_certificate_date,
                        dgcEntry.validDate.formatDateOrEmpty()
                    )
                }
                is TestCert -> {
                    certificateFilteringItemLayout.setBackgroundResource(R.color.test_certificate_background)
                    certificateFilteringItemType.setText(R.string.certificate_check_validity_test)
                    certificateFilteringItemTypeIcon.setImageResource(R.drawable.main_cert_test_white)
                    if (dgcEntry.testType == TestCert.PCR_TEST) {
                        certificateFilteringItemInfo.setText(R.string.test_certificate_detail_view_pcr_test_title)
                    } else {
                        certificateFilteringItemInfo.setText(R.string.test_certificate_detail_view_title)
                    }
                    certificateFilteringItemDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_test_certificate_date,
                        dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: ""
                    )
                }
                is Recovery -> {
                    certificateFilteringItemLayout.setBackgroundResource(R.color.info90)
                    certificateFilteringItemType.setText(R.string.certificate_check_validity_recovery)
                    certificateFilteringItemTypeIcon.setImageResource(R.drawable.main_cert_status_complete_white)
                    certificateFilteringItemInfo.setText(R.string.recovery_certificate_detail_view_title)
                    certificateFilteringItemDate.text = com.ibm.health.common.android.utils.getString(
                        R.string.certificates_overview_recovery_certificate_valid_until_date,
                        combinedCovCertificate.covCertificate.validUntil?.formatDateOrEmpty() ?: ""
                    )
                }
            }
        }
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == CANCEL_TICKETING_SECOND_CONSENT && action == DialogAction.POSITIVE) {
            findNavigator().popAll()
        }
    }

    public companion object {
        public const val CANCEL_TICKETING_SECOND_CONSENT: String = "cancel_ticketing_second_consent"
    }
}
