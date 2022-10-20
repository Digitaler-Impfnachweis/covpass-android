/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.getString
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.add.AddCovCertificateFragmentNav
import de.rki.covpass.app.boosterreissue.ReissueCallback
import de.rki.covpass.app.boosterreissue.ReissueNotificationFragmentNav
import de.rki.covpass.app.databinding.DetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.adapter.DetailAdapter
import de.rki.covpass.app.detail.adapter.DetailItem
import de.rki.covpass.app.information.FederalStateSettingFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.utils.FederalStateResolver
import de.rki.covpass.sdk.cert.models.BoosterResult
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.DGCEntry
import de.rki.covpass.sdk.cert.models.DGCEntryType
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.RecoveryCertType
import de.rki.covpass.sdk.cert.models.ReissueType
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.utils.formatDate
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatTimeOrEmpty
import de.rki.covpass.sdk.utils.isInFuture
import de.rki.covpass.sdk.utils.isOlderThan
import de.rki.covpass.sdk.utils.monthTillNow
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/**
 * Interface to communicate events from [DetailFragment] back to other fragments.
 */
internal interface DetailCallback {
    fun onDeletionCompleted()
    fun displayCert(certId: GroupedCertificatesId)
}

public interface DetailClickListener {
    public fun onShowCertificateClicked()
    public fun onNewCertificateScanClicked()
    public fun onCovCertificateClicked(id: String, dgcEntryType: DGCEntryType)
    public fun onChangeFederalStateClicked()
}

internal enum class DetailBoosterAction {
    Delete, BackPressed,
}

@Parcelize
internal class DetailFragmentNav(
    var certId: GroupedCertificatesId,
    val isFirstAdded: Boolean = false,
) : FragmentNav(DetailFragment::class)

/**
 * Fragment which shows the [GroupedCertificates] details
 * Further actions (Show QR Code, Add cov certificate)
 */
@Suppress("LargeClass")
internal class DetailFragment :
    BaseFragment(),
    DgcEntryDetailCallback,
    DetailClickListener,
    DetailEvents<DetailBoosterAction>,
    ReissueCallback {

    private val args: DetailFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { DetailViewModel<DetailBoosterAction>(scope) }
    private val binding by viewBinding(DetailBinding::inflate)
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun {
            updateViews(
                get(covpassDeps.certRepository.certs),
                FederalStateResolver.getFederalStateByCode(
                    get(commonDeps.federalStateRepository.federalState),
                )?.nameRes?.let {
                    getString(it)
                },
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val favoriteItem = menu.add(
            Menu.NONE,
            FAVORITE_ITEM_ID,
            Menu.NONE,
            if (isFavorite) R.string.accessibility_overview_certificates_label_favourites_button_active else {
                R.string.accessibility_overview_certificates_label_favourites_button_not_active
            },
        )
        val favoriteIcon = if (isFavorite) R.drawable.star_black_fill else R.drawable.star_black
        favoriteItem.setIcon(favoriteIcon)
        favoriteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == FAVORITE_ITEM_ID) {
            viewModel.onFavoriteClick(args.certId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onBackPressed(): Abortable {
        viewModel.updateHasSeenAllDetailNotification(args.certId, DetailBoosterAction.BackPressed)
        return Abort
    }

    override fun onDeletionCompleted(isGroupedCertDeleted: Boolean) {
        if (isGroupedCertDeleted) {
            viewModel.updateHasSeenAllDetailNotification(args.certId, DetailBoosterAction.Delete)
        } else {
            val dialogModel = DialogModel(
                titleRes = R.string.delete_result_dialog_header,
                messageString = getString(R.string.delete_result_dialog_message),
                positiveButtonTextRes = R.string.delete_result_dialog_positive_button_text,
            )
            showDialog(dialogModel, childFragmentManager)
            updateViews(
                covpassDeps.certRepository.certs.value,
                FederalStateResolver.getFederalStateByCode(
                    commonDeps.federalStateRepository.federalState.value,
                )?.nameRes?.let { getString(it) },
            )
        }
    }

    private fun getImmunisationStatusMessage(
        dgcEntry: DGCEntry,
        groupedCertificate: GroupedCertificates,
        immunizationStatus: ImmunizationStatus,
    ): ImmunizationInfoText {
        return when (immunizationStatus) {
            ImmunizationStatus.Full -> getFullImmunizationInfoText(
                dgcEntry,
                groupedCertificate,
            )
            ImmunizationStatus.Partial -> getPartialImmunizationInfoText(
                dgcEntry,
                groupedCertificate,
            )
            ImmunizationStatus.Invalid -> ImmunizationInfoText(
                message = getString(R.string.infschg_cert_overview_immunisation_invalid),
            )
        }
    }

    private fun updateViews(
        certList: GroupedCertificatesList,
        region: String?,
    ) {
        val certId = args.certId
        val firstAdded = args.isFirstAdded
        // Can be null after deletion... in this case no update is necessary anymore
        val groupedCertificate = certList.getGroupedCertificates(certId) ?: return
        val mainCertificate = groupedCertificate.getMainCertificate()
        isFavorite = certList.isMarkedAsFavorite(certId)
        setHasOptionsMenu(certList.certificates.size > 1)
        activity?.invalidateOptionsMenu()

        mainCertificate.covCertificate.let { cert ->
            val dgcEntry = cert.dgcEntry

            if (firstAdded) {
                sendLocalAccessibilityAnnouncementEvent(
                    getString(R.string.accessibility_scan_success_announce, cert.fullName),
                )
            }

            val isExpiredOrInvalid = when (mainCertificate.status) {
                CertValidationResult.Expired, CertValidationResult.Invalid, CertValidationResult.Revoked -> true
                CertValidationResult.ExpiryPeriod, CertValidationResult.Valid -> false
            }
            val immunizationStatus = groupedCertificate.gStatus
            val maskStatus = groupedCertificate.maskStatus
            val certStatus = mainCertificate.status
            val immunisationStatusMessage =
                getImmunisationStatusMessage(dgcEntry, groupedCertificate, immunizationStatus)
            val personalDataList = mutableListOf(
                DetailItem.Name(cert.fullName),
                DetailItem.Widget(
                    title = getString(
                        when (maskStatus) {
                            MaskStatus.NotRequired -> R.string.infschg_start_mask_optional
                            MaskStatus.Required -> R.string.infschg_start_mask_mandatory
                            MaskStatus.Invalid -> R.string.infschg_start_expired_revoked
                            MaskStatus.NoRules -> R.string.infschg_start_screen_status_grey_2
                        },
                    ),
                    statusIcon = when (maskStatus) {
                        MaskStatus.NotRequired -> R.drawable.status_mask_not_required
                        MaskStatus.Required -> R.drawable.status_mask_required
                        MaskStatus.Invalid -> R.drawable.status_mask_invalid
                        MaskStatus.NoRules -> R.drawable.status_mask_invalid
                    },
                    message = getString(
                        when (maskStatus) {
                            MaskStatus.NotRequired -> R.string.infschg_cert_overview_mask_hint_optional
                            MaskStatus.Required -> R.string.infschg_cert_overview_mask_hint_mandatory
                            MaskStatus.Invalid -> R.string.infschg_cert_overview_mask_hint_mandatory
                            MaskStatus.NoRules -> R.string.infschg_detail_page_mask_status_uncertain_copy_1
                        },
                    ),
                    link = when (maskStatus) {
                        MaskStatus.NotRequired -> R.string.infschg_detail_page_no_mask_mandatory_link
                        MaskStatus.Required -> R.string.infschg_detail_page_mask_mandatory_link
                        MaskStatus.Invalid -> R.string.infschg_detail_page_mask_status_uncertain_link
                        MaskStatus.NoRules -> R.string.infschg_detail_page_mask_status_uncertain_link
                    },
                    region = getString(R.string.infschg_start_screen_status_federal_state, region),
                    noticeMessage = getString(
                        when (maskStatus) {
                            MaskStatus.NotRequired -> R.string.infschg_detail_page_no_mask_mandatory_copy_2
                            MaskStatus.Required -> R.string.infschg_detail_page_mask_mandatory_copy_2
                            MaskStatus.Invalid -> R.string.infschg_detail_page_mask_status_uncertain_copy_2
                            MaskStatus.NoRules -> R.string.infschg_detail_page_mask_status_uncertain_copy_2
                        },
                    ),
                    subtitle = getMaskStatusInfoText(maskStatus, groupedCertificate),
                ),
                DetailItem.Widget(
                    title = getString(
                        when (immunizationStatus) {
                            ImmunizationStatus.Full -> R.string.infschg_start_immune_complete
                            ImmunizationStatus.Partial -> R.string.infschg_start_immune_incomplete
                            ImmunizationStatus.Invalid -> R.string.infschg_start_expired_revoked
                        },
                    ),
                    statusIcon = when (immunizationStatus) {
                        ImmunizationStatus.Full -> R.drawable.status_immunization_full
                        ImmunizationStatus.Partial -> R.drawable.status_immunization_partial
                        ImmunizationStatus.Invalid -> R.drawable.status_immunization_expired
                    },
                    message = immunisationStatusMessage.message,
                    subtitle = immunisationStatusMessage.date,
                ),
            )

            if (certStatus != CertValidationResult.Valid) {
                when (dgcEntry) {
                    is Vaccination -> {
                        when (dgcEntry.type) {
                            VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(
                                            R.string.certificates_overview_vaccination_certificate_message,
                                            dgcEntry.doseNumber,
                                            dgcEntry.totalSerialDoses,
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val subtitle = if (certStatus == CertValidationResult.Valid) {
                                    getString(
                                        R.string.certificate_timestamp_months,
                                        dgcEntry.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                            ?.toInstant()?.monthTillNow(),
                                    )
                                } else {
                                    null
                                }
                                val message = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> messageExpiredCert(cert.isGermanCertificate)
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_message
                                        CertValidationResult.Revoked -> messageRevokedCert(cert.isGermanCertificate)
                                        CertValidationResult.Valid ->
                                            R.string.vaccination_certificate_overview_complete_message
                                        CertValidationResult.ExpiryPeriod ->
                                            messageSoonExpiredCert(cert.isGermanCertificate)
                                    },
                                )
                                personalDataList.add(
                                    DetailItem.Widget(
                                        title = title,
                                        subtitle = subtitle,
                                        statusIcon = certStatus.getVaccinationStatusIconRes(),
                                        message = message,
                                        isExpiredOrInvalid = isExpiredOrInvalid,
                                    ),
                                )
                            }
                            VaccinationCertType.VACCINATION_COMPLETE -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(
                                            R.string.certificates_overview_vaccination_certificate_message,
                                            dgcEntry.doseNumber,
                                            dgcEntry.totalSerialDoses,
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val subtitle = if (certStatus == CertValidationResult.Valid) {
                                    getString(
                                        R.string.certificate_timestamp_months,
                                        dgcEntry.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                            ?.toInstant()?.monthTillNow(),
                                    )
                                } else {
                                    null
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(messageExpiredCert(cert.isGermanCertificate))
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.vaccination_certificate_overview_complete_from_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(messageSoonExpiredCert(cert.isGermanCertificate))
                                }
                                personalDataList.add(
                                    DetailItem.Widget(
                                        title = title,
                                        subtitle = subtitle,
                                        statusIcon = certStatus.getVaccinationStatusIconRes(),
                                        message = message,
                                        isExpiredOrInvalid = isExpiredOrInvalid,
                                    ),
                                )
                            }
                            VaccinationCertType.VACCINATION_INCOMPLETE -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(
                                            R.string.certificates_overview_vaccination_certificate_message,
                                            dgcEntry.doseNumber,
                                            dgcEntry.totalSerialDoses,
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val subtitle = if (certStatus == CertValidationResult.Valid) {
                                    getString(
                                        R.string.certificate_timestamp_months,
                                        dgcEntry.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                            ?.toInstant()?.monthTillNow(),
                                    )
                                } else {
                                    null
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(messageExpiredCert(cert.isGermanCertificate))
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.vaccination_certificate_overview_incomplete_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(messageSoonExpiredCert(cert.isGermanCertificate))
                                }
                                personalDataList.add(
                                    DetailItem.Widget(
                                        title = title,
                                        subtitle = subtitle,
                                        statusIcon = certStatus.getIncompleteVaccinationStatusIconRes(),
                                        message = message,
                                        isExpiredOrInvalid = isExpiredOrInvalid,
                                    ),
                                )
                            }
                        }
                    }
                    is TestCert -> {
                        when (dgcEntry.type) {
                            TestCertType.NEGATIVE_PCR_TEST -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(R.string.pcr_test_certificate_overview_title)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(messageExpiredCert(cert.isGermanCertificate))
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.pcr_test_certificate_overview_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(messageSoonExpiredCert(cert.isGermanCertificate))
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.pcr_test_certificate_overview_action_button_title,
                                    )
                                }
                                personalDataList.add(
                                    DetailItem.Widget(
                                        title = title,
                                        statusIcon = certStatus.getVaccinationStatusIconRes(),
                                        message = message,
                                        buttonText = buttonText,
                                        isExpiredOrInvalid = isExpiredOrInvalid,
                                    ),
                                )
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(R.string.test_certificate_overview_title)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> messageExpiredCert(cert.isGermanCertificate)
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_message
                                        CertValidationResult.Revoked -> messageRevokedCert(cert.isGermanCertificate)
                                        CertValidationResult.Valid -> R.string.test_certificate_overview_message
                                        CertValidationResult.ExpiryPeriod ->
                                            messageSoonExpiredCert(cert.isGermanCertificate)
                                    },
                                )
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.test_certificate_overview_action_button_title)
                                }
                                personalDataList.add(
                                    DetailItem.Widget(
                                        title = title,
                                        statusIcon = certStatus.getVaccinationStatusIconRes(),
                                        message = message,
                                        buttonText = buttonText,
                                        isExpiredOrInvalid = isExpiredOrInvalid,
                                    ),
                                )
                            }
                            TestCertType.POSITIVE_PCR_TEST, TestCertType.POSITIVE_ANTIGEN_TEST -> return
                        }
                    }
                    is Recovery -> {
                        val title = when (certStatus) {
                            CertValidationResult.Expired ->
                                getString(R.string.certificates_overview_expired_title)
                            CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                getString(R.string.certificates_overview_invalid_title)
                            CertValidationResult.Valid ->
                                getString(R.string.recovery_certificate_overview_valid_until_title)
                            CertValidationResult.ExpiryPeriod ->
                                getString(
                                    R.string.certificates_overview_soon_expiring_title,
                                    cert.validUntil.formatDateOrEmpty(),
                                    cert.validUntil.formatTimeOrEmpty(),
                                )
                        }
                        val message = when (certStatus) {
                            CertValidationResult.Expired ->
                                getString(messageExpiredCert(cert.isGermanCertificate))
                            CertValidationResult.Invalid ->
                                getString(R.string.certificates_overview_invalid_message)
                            CertValidationResult.Revoked ->
                                getString(messageRevokedCert(cert.isGermanCertificate))
                            CertValidationResult.Valid ->
                                getString(R.string.recovery_certificate_overview_message)
                            CertValidationResult.ExpiryPeriod ->
                                getString(messageSoonExpiredCert(cert.isGermanCertificate))
                        }
                        val buttonText = if (isExpiredOrInvalid) {
                            getString(R.string.certificates_overview_expired_action_button_title)
                        } else {
                            getString(R.string.recovery_certificate_overview_action_button_title)
                        }
                        personalDataList.add(
                            DetailItem.Widget(
                                title = title,
                                statusIcon = certStatus.getVaccinationStatusIconRes(),
                                message = message,
                                buttonText = buttonText,
                                isExpiredOrInvalid = isExpiredOrInvalid,
                            ),
                        )
                    }
                }
            }

            if (groupedCertificate.isBoosterReadyForReissue()) {
                personalDataList.add(
                    DetailItem.ReissueNotification(
                        R.string.certificate_renewal_startpage_headline,
                        R.string.certificate_renewal_startpage_copy,
                        if (!groupedCertificate.hasSeenReissueDetailNotification) {
                            R.drawable.background_new_warning
                        } else null,
                        R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new,
                        R.string.certificate_renewal_detail_view_notification_box_secondary_button,
                    ) {
                        findNavigator().push(
                            ReissueNotificationFragmentNav(
                                ReissueType.Booster,
                                groupedCertificate.getListOfIdsReadyForBoosterReissue(),
                            ),
                        )
                    },
                )
            }

            if (groupedCertificate.isExpiredReadyForReissue()) {
                if (groupedCertificate.getListOfVaccinationIdsReadyForReissue().isNotEmpty()) {
                    personalDataList.add(
                        DetailItem.ReissueNotification(
                            R.string.renewal_expiry_notification_title,
                            R.string.renewal_expiry_notification_copy_vaccination,
                            if (!groupedCertificate.hasSeenReissueDetailNotification) {
                                R.drawable.background_new_warning
                            } else null,
                            if (!groupedCertificate.hasSeenReissueDetailNotification) {
                                R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new
                            } else null,
                            R.string.renewal_expiry_notification_button_vaccination,
                        ) {
                            findNavigator().push(
                                ReissueNotificationFragmentNav(
                                    ReissueType.Vaccination,
                                    groupedCertificate.getListOfVaccinationIdsReadyForReissue(),
                                ),
                            )
                        },
                    )
                }

                groupedCertificate.getListOfRecoveryIdsReadyForReissue().forEach { recoveryId ->
                    personalDataList.add(
                        DetailItem.ReissueNotification(
                            R.string.renewal_expiry_notification_title,
                            R.string.renewal_expiry_notification_copy_recovery,
                            if (!groupedCertificate.hasSeenReissueDetailNotification) {
                                R.drawable.background_new_warning
                            } else null,
                            if (!groupedCertificate.hasSeenReissueDetailNotification) {
                                R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new
                            } else null,
                            R.string.renewal_expiry_notification_button_recovery,
                        ) {
                            findNavigator().push(
                                ReissueNotificationFragmentNav(
                                    ReissueType.Recovery,
                                    listOf(recoveryId) + groupedCertificate.getHistoricalDataForDcc(
                                        recoveryId,
                                    ),
                                ),
                            )
                        },
                    )
                }
            }

            if (groupedCertificate.boosterNotification.result == BoosterResult.Passed) {
                personalDataList.add(
                    DetailItem.BoosterNotification(
                        R.string.vaccination_certificate_overview_booster_vaccination_notification_title,
                        groupedCertificate.boosterNotification.getLocalizedDescription(),
                        groupedCertificate.boosterNotification.ruleId,
                        if (!groupedCertificate.hasSeenBoosterDetailNotification) {
                            R.drawable.background_new_warning
                        } else null,
                        if (!groupedCertificate.hasSeenBoosterDetailNotification) {
                            R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new
                        } else null,
                    ),
                )
            }

            personalDataList.addAll(
                listOf(
                    DetailItem.Header(
                        getString(R.string.certificates_overview_personal_data_title),
                        getString(R.string.certificates_overview_personal_data_title),
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_name),
                        getString(R.string.accessibility_certificates_overview_personal_data_name),
                        cert.fullNameReverse,
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_standardized_name),
                        getString(R.string.accessibility_certificates_overview_personal_data_standardized_name),
                        cert.fullTransliteratedNameReverse,
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_date_of_birth),
                        getString(R.string.accessibility_certificates_overview_personal_data_date_of_birth),
                        cert.birthDateFormatted,
                    ),
                    DetailItem.Header(
                        getString(R.string.certificates_overview_all_certificates_title),
                        getString(R.string.accessibility_certificates_overview_all_certificates_title),
                    ),
                    DetailItem.Infobox(
                        getString(R.string.certificates_overview_all_certificates_app_reference_title),
                        getString(R.string.certificates_overview_all_certificates_app_reference_text),
                    ),
                ),
            )

            val sortedCertificatesList = groupedCertificate.getSortedCertificates().mapNotNull {
                when (val groupedDgcEntry = it.covCertificate.dgcEntry) {
                    is Vaccination -> {
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_vaccination_certificate_title),
                            subtitle = getString(
                                R.string.certificates_overview_vaccination_certificate_message,
                                groupedDgcEntry.doseNumber,
                                groupedDgcEntry.totalSerialDoses,
                            ),
                            date = getString(
                                R.string.certificates_overview_vaccination_certificate_date,
                                groupedDgcEntry.occurrence?.formatDate(),
                            ),
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = it.status,
                        )
                    }
                    is TestCert -> {
                        when (groupedDgcEntry.type) {
                            TestCertType.NEGATIVE_PCR_TEST -> {
                                DetailItem.Certificate(
                                    id = groupedDgcEntry.id,
                                    type = groupedDgcEntry.type,
                                    title = getString(R.string.certificates_overview_test_certificate_title),
                                    subtitle = getString(R.string.certificates_overview_pcr_test_certificate_message),
                                    date = getString(
                                        R.string.certificates_overview_test_certificate_date,
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()
                                            ?.formatDateTime(),
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = it.status,
                                )
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                DetailItem.Certificate(
                                    id = groupedDgcEntry.id,
                                    type = groupedDgcEntry.type,
                                    title = getString(R.string.certificates_overview_test_certificate_title),
                                    subtitle = getString(R.string.certificates_overview_test_certificate_message),
                                    date = getString(
                                        R.string.certificates_overview_test_certificate_date,
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()
                                            ?.formatDateTime(),
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = it.status,
                                )
                            }
                            TestCertType.POSITIVE_PCR_TEST, TestCertType.POSITIVE_ANTIGEN_TEST -> null
                        }
                    }
                    is Recovery -> {
                        val date = if (groupedDgcEntry.validFrom.isInFuture()) {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_from_date,
                                groupedDgcEntry.validFrom?.formatDateOrEmpty(),
                            )
                        } else {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_until_date,
                                groupedDgcEntry.validUntil?.formatDateOrEmpty(),
                            )
                        }
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_recovery_certificate_title),
                            subtitle = getString(R.string.certificates_overview_recovery_certificate_message),
                            date = date,
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = it.status,
                        )
                    }
                }
            }
            DetailAdapter(personalDataList + sortedCertificatesList, this, this)
                .attachTo(binding.detailVaccinationList)
        }
    }

    private fun messageRevokedCert(isGermanCertificate: Boolean) =
        if (isGermanCertificate) {
            R.string.revocation_detail_single_DE
        } else {
            R.string.revocation_detail_single_notDE
        }

    private fun messageExpiredCert(isGermanCertificate: Boolean) =
        if (isGermanCertificate) {
            R.string.certificates_overview_expired_message
        } else {
            R.string.certificates_overview_expired_or_soon_expiring_nonDE
        }

    private fun messageSoonExpiredCert(isGermanCertificate: Boolean) =
        if (isGermanCertificate) {
            R.string.certificates_overview_soon_expiring_subtitle
        } else {
            R.string.certificates_overview_expired_or_soon_expiring_nonDE
        }

    override fun onShowCertificateClicked() {
        findNavigator().pop()
    }

    override fun onNewCertificateScanClicked() {
        findNavigator().push(AddCovCertificateFragmentNav())
    }

    override fun onCovCertificateClicked(id: String, dgcEntryType: DGCEntryType) {
        when (dgcEntryType) {
            VaccinationCertType.VACCINATION_INCOMPLETE,
            VaccinationCertType.VACCINATION_COMPLETE,
            VaccinationCertType.VACCINATION_FULL_PROTECTION,
            -> {
                findNavigator().push(VaccinationDetailFragmentNav(id))
            }
            TestCertType.NEGATIVE_PCR_TEST,
            TestCertType.NEGATIVE_ANTIGEN_TEST,
            -> {
                findNavigator().push(TestDetailFragmentNav(id))
            }
            RecoveryCertType.RECOVERY -> {
                findNavigator().push(RecoveryDetailFragmentNav(id))
            }
            TestCertType.POSITIVE_PCR_TEST,
            TestCertType.POSITIVE_ANTIGEN_TEST,
            -> return
            // .let{} to enforce exhaustiveness
        }.let {}
    }

    override fun onChangeFederalStateClicked() {
        findNavigator().push(FederalStateSettingFragmentNav())
    }

    private fun setupActionBar() {
        attachToolbar(binding.detailToolbar)
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow
                setHomeAsUpIndicator(icon)
                setHomeActionContentDescription(R.string.accessibility_overview_certificates_label_back)
            }
        }
    }

    private companion object {
        private const val FAVORITE_ITEM_ID = 82957
    }

    override fun onHasSeenAllDetailNotificationUpdated(tag: DetailBoosterAction) {
        when (tag) {
            DetailBoosterAction.Delete -> {
                findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
            }
            DetailBoosterAction.BackPressed -> {
                findNavigator().popUntil<DetailCallback>()?.displayCert(args.certId)
            }
        }.let {}
    }

    private fun sendLocalAccessibilityAnnouncementEvent(accessibilityString: String) {
        val manager =
            context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (manager.isEnabled) {
            val accessibilityEvent = AccessibilityEvent.obtain()
            accessibilityEvent.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            accessibilityEvent.className = javaClass.name
            accessibilityEvent.packageName = requireContext().packageName
            accessibilityEvent.text.add(accessibilityString)
            manager.sendAccessibilityEvent(accessibilityEvent)
        }
    }

    override fun onReissueCancel() {}

    override fun onReissueFinish(certificatesId: GroupedCertificatesId?) {
        updateViews(
            covpassDeps.certRepository.certs.value,
            FederalStateResolver.getFederalStateByCode(
                commonDeps.federalStateRepository.federalState.value,
            )?.nameRes?.let { getString(it) },
        )
    }

    private fun CertValidationResult.getVaccinationStatusIconRes(): Int {
        return if (this == CertValidationResult.Valid) {
            R.drawable.status_immunization_full
        } else if (this == CertValidationResult.ExpiryPeriod) {
            R.drawable.detail_cert_status_expiring
        } else {
            R.drawable.detail_cert_status_expired
        }
    }

    private fun CertValidationResult.getIncompleteVaccinationStatusIconRes(): Int {
        return if (this == CertValidationResult.Valid) {
            R.drawable.detail_cert_status_incomplete
        } else if (this == CertValidationResult.ExpiryPeriod) {
            R.drawable.detail_cert_status_expiring
        } else {
            R.drawable.detail_cert_status_expired
        }
    }

    private fun getFullImmunizationInfoText(
        dgcEntry: DGCEntry,
        groupedCertificate: GroupedCertificates,
    ): ImmunizationInfoText {
        val latestRecovery = groupedCertificate.getLatestValidRecovery()
        val recovery = latestRecovery?.covCertificate?.dgcEntry as? Recovery
        return when (dgcEntry) {
            is Vaccination -> {
                when {
                    dgcEntry.doseNumber > 3 -> {
                        ImmunizationInfoText(
                            message = getString(R.string.infschg_cert_overview_immunisation_complete_B2),
                            date = getString(
                                R.string.infschg_cert_overview_immunisation_time_since,
                                dgcEntry.occurrence.formatDateOrEmpty(),
                            ),
                        )
                    }
                    dgcEntry.doseNumber == 3 -> {
                        ImmunizationInfoText(
                            message = getString(R.string.infschg_cert_overview_immunisation_third_vacc_C2),
                            date = getString(
                                R.string.infschg_cert_overview_immunisation_time_since,
                                dgcEntry.occurrence.formatDateOrEmpty(),
                            ),
                        )
                    }
                    dgcEntry.doseNumber == 2 && latestRecovery != null &&
                        dgcEntry.occurrence?.isAfter(recovery?.firstResult) == true -> {
                        ImmunizationInfoText(
                            message = getString(R.string.infschg_cert_overview_immunisation_E2),
                            date = getString(
                                R.string.infschg_cert_overview_immunisation_time_since,
                                dgcEntry.occurrence.formatDateOrEmpty(),
                            ),
                        )
                    }
                    dgcEntry.doseNumber == 2 && latestRecovery != null &&
                        LocalDate.now().isAfter(recovery?.firstResult?.plusDays(29)) -> {
                        ImmunizationInfoText(
                            message = getString(R.string.infschg_cert_overview_immunisation_E2),
                            date = getString(
                                R.string.infschg_cert_overview_immunisation_time_since,
                                recovery?.firstResult?.plusDays(29).formatDateOrEmpty(),
                            ),
                        )
                    }
                    else ->
                        ImmunizationInfoText(
                            message = getString(R.string.infschg_cert_overview_immunisation_incomplete_A),
                        )
                }
            }
            else -> {
                ImmunizationInfoText(
                    message = getString(R.string.infschg_cert_overview_immunisation_incomplete_A),
                )
            }
        }
    }

    private fun getPartialImmunizationInfoText(
        dgcEntry: DGCEntry,
        groupedCertificate: GroupedCertificates,
    ): ImmunizationInfoText {
        val latestRecovery = groupedCertificate.getLatestValidRecovery()
        val recovery = latestRecovery?.covCertificate?.dgcEntry as? Recovery
        return if (
            dgcEntry is Vaccination && dgcEntry.doseNumber == 2 && recovery != null &&
            LocalDate.now().isBefore(recovery.firstResult?.plusDays(29))
        ) {
            ImmunizationInfoText(
                message = getString(
                    R.string.infschg_cert_overview_immunisation_E22,
                    Duration.between(
                        recovery.firstResult?.plusDays(29)?.atStartOfDay(),
                        LocalDate.now().atStartOfDay(),
                    ).toDays(),
                ),
                date = getString(
                    R.string.infschg_cert_overview_immunisation_time_from,
                    recovery.firstResult?.plusDays(29).formatDateOrEmpty(),
                ),
            )
        } else {
            ImmunizationInfoText(
                message = getString(R.string.infschg_cert_overview_immunisation_incomplete_A),
            )
        }
    }
}

private fun getMaskStatusInfoText(
    maskStatus: MaskStatus,
    groupedCertificate: GroupedCertificates,
): String? {
    val latestVaccination = groupedCertificate.getLatestValidVaccination()
    val latestRecovery = groupedCertificate.getLatestValidRecovery()
    val vaccination = latestVaccination?.covCertificate?.dgcEntry as? Vaccination
    val recovery = latestRecovery?.covCertificate?.dgcEntry as? Recovery
    return when (maskStatus) {
        MaskStatus.NotRequired -> maskNotRequiredStatusInfoText(vaccination, recovery)
        MaskStatus.Required -> maskRequiredStatusInfoText(vaccination, recovery)
        else -> null
    }
}

private fun maskNotRequiredStatusInfoText(
    vaccination: Vaccination?,
    recovery: Recovery?,
): String? = when {
    vaccination != null -> {
        getString(
            R.string.infschg_cert_overview_mask_time_until,
            vaccination.occurrence?.plusDays(90).formatDateOrEmpty(),
        )
    }
    recovery != null -> {
        getString(
            R.string.infschg_cert_overview_mask_time_until,
            recovery.firstResult?.plusDays(90).formatDateOrEmpty(),
        )
    }
    else -> null
}

private fun maskRequiredStatusInfoText(
    vaccination: Vaccination?,
    recovery: Recovery?,
): String? = when {
    vaccination != null && recovery != null -> {
        if (
            recovery.firstResult?.isAfter(vaccination.occurrence) == true &&
            recovery.firstResult?.isOlderThan(29) == true
        ) {
            getString(
                R.string.infschg_cert_overview_mask_time_from,
                recovery.firstResult?.plusDays(28).formatDateOrEmpty(),
            )
        } else {
            null
        }
    }
    vaccination != null -> {
        getString(
            R.string.infschg_cert_overview_mask_time_from,
            vaccination.occurrence.formatDateOrEmpty(),
        )
    }
    recovery != null -> {
        getString(
            R.string.infschg_cert_overview_mask_time_from,
            recovery.firstResult?.plusDays(28).formatDateOrEmpty(),
        )
    }
    else -> null
}

public data class ImmunizationInfoText(
    val message: String,
    val date: String? = null,
)
