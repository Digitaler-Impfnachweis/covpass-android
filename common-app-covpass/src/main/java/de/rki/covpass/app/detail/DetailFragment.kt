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
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.add.AddCovCertificateFragmentNav
import de.rki.covpass.app.boosterreissue.ReissueCallback
import de.rki.covpass.app.boosterreissue.ReissueConsentFragmentNav
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
import de.rki.covpass.sdk.cert.models.DGCEntryType
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.cert.models.ImmunizationStatus
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.RecoveryCertType
import de.rki.covpass.sdk.cert.models.ReissueState
import de.rki.covpass.sdk.cert.models.ReissueType
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.cert.models.isExpired
import de.rki.covpass.sdk.utils.formatDate
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatTimeOrEmpty
import de.rki.covpass.sdk.utils.isInFuture
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.parcelize.Parcelize

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
    var groupedCertificatesId: GroupedCertificatesId,
    val certId: String? = null,
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
    private val viewModel by reactiveState {
        DetailViewModel<DetailBoosterAction>(
            scope,
            args.groupedCertificatesId,
            args.isFirstAdded,
            args.certId,
        )
    }
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
                get(viewModel.maskRuleValidFrom),
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
            viewModel.onFavoriteClick(args.groupedCertificatesId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onBackPressed(): Abortable {
        viewModel.updateHasSeenAllDetailNotification(
            args.groupedCertificatesId,
            DetailBoosterAction.BackPressed,
        )
        return Abort
    }

    override fun onDeletionCompleted(isGroupedCertDeleted: Boolean) {
        if (isGroupedCertDeleted) {
            viewModel.updateHasSeenAllDetailNotification(
                args.groupedCertificatesId,
                DetailBoosterAction.Delete,
            )
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
                viewModel.maskRuleValidFrom.value,
            )
        }
    }

    private fun updateViews(
        certList: GroupedCertificatesList,
        region: String?,
        ruleValidFromDate: String?,
    ) {
        val certId = args.groupedCertificatesId
        val firstAdded = args.isFirstAdded
        // Can be null after deletion... in this case no update is necessary anymore
        val groupedCertificate = certList.getGroupedCertificates(certId) ?: return
        val mainCertificate = groupedCertificate.getMainCertificate()
        isFavorite = certList.isMarkedAsFavorite(certId)
        setHasOptionsMenu(certList.certificates.size > 1)
        activity?.invalidateOptionsMenu()

        mainCertificate.covCertificate.let { cert ->
            if (firstAdded) {
                sendLocalAccessibilityAnnouncementEvent(
                    getString(R.string.accessibility_scan_success_announce, cert.fullName),
                )
            }

            val immunizationStatusWrapper = groupedCertificate.immunizationStatusWrapper
            val maskStatusWrapper = groupedCertificate.maskStatusWrapper
            val maskStatus = if (viewModel.maskRuleValidFrom.value?.isNotBlank() == true && cert.isExpired()) {
                maskStatusWrapper.maskStatus
            } else {
                MaskStatus.NoRules
            }
            val personalDataList: MutableList<DetailItem> = mutableListOf(
                DetailItem.Name(cert.fullName),
            )
            if (groupedCertificate.isExpiredReadyForReissue()) {
                if (groupedCertificate.getListOfVaccinationIdsReadyForReissue().isNotEmpty()) {
                    val vaccination = groupedCertificate.certificates.find {
                        it.covCertificate.dgcEntry.id ==
                            groupedCertificate.getListOfVaccinationIdsReadyForReissue().first()
                    }
                    personalDataList.add(
                        DetailItem.ReissueNotification(
                            when (vaccination?.reissueState) {
                                ReissueState.Ready,
                                ReissueState.NotGermanReady,
                                ReissueState.AfterTimeLimit,
                                -> {
                                    if (vaccination.status == CertValidationResult.Expired) {
                                        R.string.renewal_bluebox_title_expired_vaccination
                                    } else {
                                        R.string.renewal_bluebox_title_expiring_soon_vaccination
                                    }
                                }
                                else ->
                                    R.string.renewal_bluebox_title_expiring_soon_vaccination
                            },
                            when (vaccination?.reissueState) {
                                ReissueState.Ready -> {
                                    if (vaccination.status == CertValidationResult.Expired) {
                                        getString(
                                            R.string.renewal_bluebox_copy_expired,
                                            vaccination.covCertificate.validUntil.formatDateOrEmpty(),
                                            vaccination.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    } else {
                                        getString(
                                            R.string.renewal_bluebox_copy_expiring_soon,
                                            vaccination.covCertificate.validUntil.formatDateOrEmpty(),
                                            vaccination.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    }
                                }
                                ReissueState.NotGermanReady ->
                                    if (vaccination.status == CertValidationResult.Expired) {
                                        getString(R.string.renewal_bluebox_copy_expiry_not_german)
                                    } else {
                                        getString(
                                            R.string.renewal_bluebox_copy_expiring_soon_not_german,
                                            vaccination.covCertificate.validUntil.formatDateOrEmpty(),
                                            vaccination.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    }
                                ReissueState.AfterTimeLimit -> {
                                    getString(
                                        R.string.renewal_bluebox_copy_expiry_not_available,
                                        vaccination.covCertificate.validUntil.formatDateOrEmpty(),
                                        vaccination.covCertificate.validUntil.formatTimeOrEmpty(),
                                    )
                                }
                                else ->
                                    getString(
                                        R.string.renewal_bluebox_copy_expiry_not_available,
                                        vaccination?.covCertificate?.validUntil.formatDateOrEmpty(),
                                        vaccination?.covCertificate?.validUntil.formatTimeOrEmpty(),
                                    )
                            },
                            null,
                            null,
                            R.string.renewal_expiry_notification_button_vaccination,
                            vaccination?.reissueState == ReissueState.Ready,
                        ) {
                            findNavigator().push(
                                ReissueConsentFragmentNav(
                                    groupedCertificate.getListOfVaccinationIdsReadyForReissue(),
                                    ReissueType.Vaccination,
                                ),
                            )
                        },
                    )
                }

                groupedCertificate.getListOfRecoveryIdsReadyForReissue().forEach { recoveryId ->
                    val recovery = groupedCertificate.certificates.find {
                        it.covCertificate.dgcEntry.id == recoveryId
                    }
                    personalDataList.add(
                        DetailItem.ReissueNotification(
                            when (recovery?.reissueState) {
                                ReissueState.Ready -> {
                                    if (recovery.status == CertValidationResult.Expired) {
                                        R.string.renewal_bluebox_title_expired_recovery
                                    } else {
                                        R.string.renewal_bluebox_title_expiring_soon_recovery
                                    }
                                }
                                else ->
                                    R.string.renewal_bluebox_title_expiring_soon_recovery
                            },
                            when (recovery?.reissueState) {
                                ReissueState.Ready -> {
                                    if (recovery.status == CertValidationResult.Expired) {
                                        getString(
                                            R.string.renewal_bluebox_copy_expired,
                                            recovery.covCertificate.validUntil.formatDateOrEmpty(),
                                            recovery.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    } else {
                                        getString(
                                            R.string.renewal_bluebox_copy_expiring_soon,
                                            recovery.covCertificate.validUntil.formatDateOrEmpty(),
                                            recovery.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    }
                                }
                                ReissueState.NotGermanReady ->
                                    if (recovery.status == CertValidationResult.Expired) {
                                        getString(R.string.renewal_bluebox_copy_expiry_not_german)
                                    } else {
                                        getString(
                                            R.string.renewal_bluebox_copy_expiring_soon_not_german,
                                            recovery.covCertificate.validUntil.formatDateOrEmpty(),
                                            recovery.covCertificate.validUntil.formatTimeOrEmpty(),
                                        )
                                    }
                                else ->
                                    getString(
                                        R.string.renewal_bluebox_copy_expiry_not_available,
                                        recovery?.covCertificate?.validUntil.formatDateOrEmpty(),
                                        recovery?.covCertificate?.validUntil.formatTimeOrEmpty(),
                                    )
                            },
                            null,
                            null,
                            R.string.renewal_expiry_notification_button_recovery,
                            recovery?.reissueState == ReissueState.Ready,
                        ) {
                            findNavigator().push(
                                ReissueConsentFragmentNav(
                                    listOf(recoveryId) + groupedCertificate.getHistoricalDataForDcc(
                                        recoveryId,
                                    ),
                                    ReissueType.Recovery,
                                ),
                            )
                        },
                    )
                }
            }

            personalDataList.add(
                DetailItem.Widget(
                    title = getString(
                        when (maskStatus) {
                            MaskStatus.NotRequired -> R.string.infschg_detail_page_no_mask_mandatory_title
                            MaskStatus.Required -> R.string.infschg_detail_page_mask_mandatory_title
                            MaskStatus.Invalid -> if (
                                mainCertificate.status == CertValidationResult.Expired &&
                                mainCertificate.reissueState == ReissueState.AfterTimeLimit
                            ) {
                                R.string.infschg_detail_view_status_not_applicable_title
                            } else {
                                R.string.infschg_detail_page_no_valid_certificate_title
                            }
                            MaskStatus.NoRules -> R.string.infschg_start_screen_status_grey_2
                        },
                    ),
                    statusIcon = when (maskStatus) {
                        MaskStatus.NotRequired -> R.drawable.status_mask_not_required
                        MaskStatus.Required -> R.drawable.status_mask_required
                        MaskStatus.Invalid -> R.drawable.status_mask_invalid
                        MaskStatus.NoRules -> R.drawable.status_mask_invalid
                    },
                    message = when (maskStatus) {
                        MaskStatus.NotRequired ->
                            getString(
                                R.string.infschg_detail_page_no_mask_mandatory_copy_1,
                                ruleValidFromDate,
                            )
                        MaskStatus.Required ->
                            getString(
                                R.string.infschg_detail_page_mask_mandatory_copy_1,
                                ruleValidFromDate,
                            )
                        MaskStatus.Invalid -> if (
                            mainCertificate.status == CertValidationResult.Expired &&
                            mainCertificate.reissueState == ReissueState.AfterTimeLimit
                        ) {
                            getString(R.string.infschg_detail_view_status_not_applicable_copy)
                        } else {
                            getString(R.string.infschg_detail_page_no_valid_certificate_copy)
                        }
                        MaskStatus.NoRules ->
                            getString(R.string.infschg_detail_page_mask_status_uncertain_copy_1)
                    },
                    link = when (maskStatus) {
                        MaskStatus.NotRequired -> R.string.infschg_detail_page_no_mask_mandatory_link
                        MaskStatus.Required -> R.string.infschg_detail_page_mask_mandatory_link
                        MaskStatus.Invalid -> R.string.infschg_detail_page_no_valid_certificate_link
                        MaskStatus.NoRules -> R.string.infschg_detail_page_mask_status_uncertain_link
                    },
                    region = when (maskStatus) {
                        MaskStatus.NotRequired,
                        MaskStatus.Required,
                        -> getString(
                            R.string.infschg_detail_page_mask_mandatory_federal_state,
                            region,
                        )
                        MaskStatus.Invalid -> null
                        MaskStatus.NoRules,
                        -> getString(
                            R.string.infschg_detail_page_mask_status_uncertain_federal_state,
                            region,
                        )
                    },
                    noticeMessage = if (
                        mainCertificate.status == CertValidationResult.Expired &&
                        mainCertificate.reissueState == ReissueState.AfterTimeLimit &&
                        maskStatus != MaskStatus.NoRules
                    ) {
                        null
                    } else {
                        getString(
                            when (maskStatus) {
                                MaskStatus.NotRequired -> R.string.infschg_detail_page_no_mask_mandatory_copy_2
                                MaskStatus.Required -> R.string.infschg_detail_page_mask_mandatory_copy_2
                                MaskStatus.Invalid -> R.string.infschg_detail_page_mask_status_uncertain_copy_2
                                MaskStatus.NoRules -> R.string.infschg_detail_page_mask_status_uncertain_copy_2
                            },
                        )
                    },
                    subtitle = if (maskStatusWrapper.additionalDate.isNotEmpty()) {
                        when (maskStatus) {
                            MaskStatus.Required -> {
                                getString(
                                    R.string.infschg_cert_overview_mask_time_from,
                                    maskStatusWrapper.additionalDate,
                                )
                            }
                            MaskStatus.NotRequired -> {
                                getString(
                                    R.string.infschg_cert_overview_mask_time_until,
                                    maskStatusWrapper.additionalDate,
                                )
                            }
                            else -> null
                        }
                    } else {
                        null
                    },
                ),
            )

            if (immunizationStatusWrapper.immunizationStatus != ImmunizationStatus.Invalid) {
                personalDataList.add(
                    DetailItem.Widget(
                        title = getString(
                            when (immunizationStatusWrapper.immunizationStatus) {
                                ImmunizationStatus.Full -> R.string.infschg_start_immune_complete
                                ImmunizationStatus.Partial -> R.string.infschg_start_immune_incomplete
                                else -> R.string.infschg_start_expired_revoked
                            },
                        ),
                        statusIcon = when (immunizationStatusWrapper.immunizationStatus) {
                            ImmunizationStatus.Full -> R.drawable.status_immunization_full
                            ImmunizationStatus.Partial -> R.drawable.status_immunization_partial
                            ImmunizationStatus.Invalid -> R.drawable.status_immunization_expired
                        },
                        message = when (immunizationStatusWrapper.immunizationStatus) {
                            ImmunizationStatus.Full -> immunizationStatusWrapper.immunizationText
                            ImmunizationStatus.Partial -> {
                                immunizationStatusWrapper.immunizationText.ifEmpty {
                                    getString(R.string.infschg_cert_overview_immunisation_incomplete_A)
                                }
                            }
                            ImmunizationStatus.Invalid ->
                                getString(R.string.infschg_cert_overview_immunisation_invalid)
                        },
                        subtitle = if (immunizationStatusWrapper.fullImmunityBasedOnRecoveryDate.isNotEmpty()) {
                            getString(
                                R.string.infschg_cert_overview_immunisation_time_from,
                                immunizationStatusWrapper.fullImmunityBasedOnRecoveryDate,
                            )
                        } else {
                            null
                        },
                        isOneElementForScreenReader = true,
                    ),
                )
            }

            if (groupedCertificate.isBoosterReadyForReissue()) {
                personalDataList.add(
                    DetailItem.ReissueNotification(
                        R.string.certificate_renewal_startpage_headline,
                        getString(R.string.certificate_renewal_startpage_copy),
                        if (!groupedCertificate.hasSeenReissueDetailNotification) {
                            R.drawable.background_new_warning
                        } else null,
                        R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new,
                        R.string.certificate_renewal_detail_view_notification_box_secondary_button,
                        true,
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
                            showReissueTitle = it.reissueState.isReadyForReissue(
                                groupedCertificate.isExpiredReadyForReissue(),
                            ),
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
                                R.string.certificates_overview_recovery_certificate_sample_date,
                                groupedDgcEntry.firstResult?.formatDateOrEmpty(),
                            )
                        }
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_recovery_certificate_title),
                            subtitle = getString(R.string.certificates_overview_recovery_certificate_message),
                            date = date,
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            showReissueTitle = it.reissueState.isReadyForReissue(
                                groupedCertificate.isExpiredReadyForReissue(),
                            ),
                            certStatus = it.status,
                        )
                    }
                }
            }
            DetailAdapter(personalDataList + sortedCertificatesList, this, this)
                .attachTo(binding.detailVaccinationList)
        }
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
                findNavigator().popUntil<DetailCallback>()?.displayCert(args.groupedCertificatesId)
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
            viewModel.maskRuleValidFrom.value,
        )
    }

    private fun ReissueState.isReadyForReissue(isMainCertReadyForReissue: Boolean): Boolean =
        this == ReissueState.Ready && isMainCertReadyForReissue

    override fun onOpenReissue(reissueType: ReissueType, listCertIds: List<String>) {
        findNavigator().push(ReissueNotificationFragmentNav(reissueType, listCertIds))
    }
}
