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
import de.rki.covpass.app.boosterreissue.ReissueNotificationFragmentNav
import de.rki.covpass.app.databinding.DetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.adapter.DetailAdapter
import de.rki.covpass.app.detail.adapter.DetailItem
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.*
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
internal class DetailFragment :
    BaseFragment(), DgcEntryDetailCallback, DetailClickListener, DetailEvents<DetailBoosterAction>, ReissueCallback {

    private val args: DetailFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { DetailViewModel<DetailBoosterAction>(scope) }
    private val binding by viewBinding(DetailBinding::inflate)
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val favoriteItem = menu.add(
            Menu.NONE,
            FAVORITE_ITEM_ID,
            Menu.NONE,
            if (isFavorite) R.string.accessibility_overview_certificates_label_favourites_button_active else
                R.string.accessibility_overview_certificates_label_favourites_button_not_active
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
            updateViews(covpassDeps.certRepository.certs.value)
        }
    }

    private fun updateViews(certList: GroupedCertificatesList) {
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
                    getString(R.string.accessibility_scan_success_announce, cert.fullName)
                )
            }

            val isExpiredOrInvalid = when (mainCertificate.status) {
                CertValidationResult.Expired, CertValidationResult.Invalid, CertValidationResult.Revoked -> true
                CertValidationResult.ExpiryPeriod, CertValidationResult.Valid -> false
            }
            val certStatus = mainCertificate.status
            val personalDataList = mutableListOf(
                DetailItem.Name(cert.fullName),
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
                                        getString(R.string.vaccination_certificate_overview_complete_title)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> R.string.certificates_overview_expired_message
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_message
                                        CertValidationResult.Revoked -> messageRevokedCert(cert.isGermanCertificate)
                                        CertValidationResult.Valid ->
                                            R.string.vaccination_certificate_overview_complete_message
                                        CertValidationResult.ExpiryPeriod ->
                                            R.string.certificates_overview_soon_expiring_subtitle
                                    }
                                )
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.vaccination_certificate_overview_complete_action_button_title)
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid,
                                        CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid -> R.drawable.detail_cert_status_complete
                                        CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
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
                                            R.string.vaccination_certificate_overview_complete_from_title,
                                            mainCertificate.covCertificate.validDate.formatDateOrEmpty()
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.vaccination_certificate_overview_complete_from_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.certificates_overview_soon_expiring_subtitle)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.vaccination_certificate_overview_complete_action_button_title)
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid,
                                        CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid -> R.drawable.detail_cert_status_complete
                                        CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
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
                                            R.string.vaccination_certificate_overview_incomplete_title,
                                            dgcEntry.doseNumber,
                                            dgcEntry.totalSerialDoses
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.vaccination_certificate_overview_incomplete_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.certificates_overview_soon_expiring_subtitle)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.vaccination_certificate_detail_view_incomplete_action_button_title
                                    )
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid,
                                        CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid -> R.drawable.detail_cert_status_incomplete
                                        CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
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
                                        getString(
                                            R.string.pcr_test_certificate_overview_title,
                                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_message)
                                    CertValidationResult.Invalid ->
                                        getString(R.string.certificates_overview_invalid_message)
                                    CertValidationResult.Revoked ->
                                        getString(messageRevokedCert(cert.isGermanCertificate))
                                    CertValidationResult.Valid ->
                                        getString(R.string.pcr_test_certificate_overview_message)
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(R.string.certificates_overview_soon_expiring_subtitle)
                                }
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(
                                        R.string.pcr_test_certificate_overview_action_button_title
                                    )
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid,
                                        CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid -> R.drawable.detail_cert_status_complete
                                        CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
                                )
                            }
                            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                                val title = when (certStatus) {
                                    CertValidationResult.Expired ->
                                        getString(R.string.certificates_overview_expired_title)
                                    CertValidationResult.Invalid, CertValidationResult.Revoked ->
                                        getString(R.string.certificates_overview_invalid_title)
                                    CertValidationResult.Valid ->
                                        getString(
                                            R.string.test_certificate_overview_title,
                                            dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                        )
                                    CertValidationResult.ExpiryPeriod ->
                                        getString(
                                            R.string.certificates_overview_soon_expiring_title,
                                            cert.validUntil.formatDateOrEmpty(),
                                            cert.validUntil.formatTimeOrEmpty(),
                                        )
                                }
                                val message = getString(
                                    when (certStatus) {
                                        CertValidationResult.Expired -> R.string.certificates_overview_expired_message
                                        CertValidationResult.Invalid -> R.string.certificates_overview_invalid_message
                                        CertValidationResult.Revoked -> messageRevokedCert(cert.isGermanCertificate)
                                        CertValidationResult.Valid -> R.string.test_certificate_overview_message
                                        CertValidationResult.ExpiryPeriod ->
                                            R.string.certificates_overview_soon_expiring_subtitle
                                    }
                                )
                                val buttonText = if (isExpiredOrInvalid) {
                                    getString(R.string.certificates_overview_expired_action_button_title)
                                } else {
                                    getString(R.string.test_certificate_overview_action_button_title)
                                }
                                DetailItem.Widget(
                                    title = title,
                                    statusIcon = when (certStatus) {
                                        CertValidationResult.Expired,
                                        CertValidationResult.Invalid,
                                        CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                        CertValidationResult.Valid -> R.drawable.detail_cert_status_complete
                                        CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                                    },
                                    message = message,
                                    buttonText = buttonText,
                                    isExpiredOrInvalid = isExpiredOrInvalid
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
                                if (dgcEntry.validFrom.isInFuture()) {
                                    getString(
                                        R.string.recovery_certificate_overview_valid_from_title,
                                        dgcEntry.validFrom?.formatDateOrEmpty()
                                    )
                                } else {
                                    getString(
                                        R.string.recovery_certificate_overview_valid_until_title,
                                        dgcEntry.validUntil?.formatDateOrEmpty()
                                    )
                                }
                            CertValidationResult.ExpiryPeriod ->
                                getString(
                                    R.string.certificates_overview_soon_expiring_title,
                                    cert.validUntil.formatDateOrEmpty(),
                                    cert.validUntil.formatTimeOrEmpty(),
                                )
                        }
                        val message = when (certStatus) {
                            CertValidationResult.Expired ->
                                getString(R.string.certificates_overview_expired_message)
                            CertValidationResult.Invalid ->
                                getString(R.string.certificates_overview_invalid_message)
                            CertValidationResult.Revoked ->
                                getString(messageRevokedCert(cert.isGermanCertificate))
                            CertValidationResult.Valid ->
                                getString(R.string.recovery_certificate_overview_message)
                            CertValidationResult.ExpiryPeriod ->
                                getString(R.string.certificates_overview_soon_expiring_subtitle)
                        }
                        val buttonText = if (isExpiredOrInvalid) {
                            getString(R.string.certificates_overview_expired_action_button_title)
                        } else {
                            getString(R.string.recovery_certificate_overview_action_button_title)
                        }
                        DetailItem.Widget(
                            title = title,
                            statusIcon = when (certStatus) {
                                CertValidationResult.Expired,
                                CertValidationResult.Invalid,
                                CertValidationResult.Revoked -> R.drawable.detail_cert_status_expired
                                CertValidationResult.Valid -> R.drawable.detail_cert_status_complete
                                CertValidationResult.ExpiryPeriod -> R.drawable.detail_cert_status_expiring
                            },
                            message = message,
                            buttonText = buttonText,
                            isExpiredOrInvalid = isExpiredOrInvalid
                        )
                    }
                },
            )

            if (groupedCertificate.isReadyForReissue()) {
                personalDataList.add(
                    DetailItem.ReissueNotification(
                        R.string.certificate_renewal_startpage_headline,
                        R.string.certificate_renewal_startpage_copy,
                        if (!groupedCertificate.hasSeenReissueDetailNotification) {
                            R.drawable.background_new_warning
                        } else null,
                        R.string.vaccination_certificate_overview_booster_vaccination_notification_icon_new,
                        R.string.certificate_renewal_detail_view_notification_box_secondary_button
                    ) {
                        findNavigator().push(
                            ReissueNotificationFragmentNav(groupedCertificate.getListOfIdsReadyForReissue())
                        )
                    }
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
                        } else null
                    )
                )
            }

            personalDataList.addAll(
                listOf(
                    DetailItem.Header(
                        getString(R.string.certificates_overview_personal_data_title),
                        getString(R.string.certificates_overview_personal_data_title)
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_name),
                        getString(R.string.accessibility_certificates_overview_personal_data_name),
                        cert.fullNameReverse
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_standardized_name),
                        getString(R.string.accessibility_certificates_overview_personal_data_standardized_name),
                        cert.fullTransliteratedNameReverse
                    ),
                    DetailItem.Personal(
                        getString(R.string.certificates_overview_personal_data_date_of_birth),
                        getString(R.string.accessibility_certificates_overview_personal_data_date_of_birth),
                        cert.birthDateFormatted
                    ),
                    DetailItem.Header(
                        getString(R.string.certificates_overview_all_certificates_title),
                        getString(R.string.accessibility_certificates_overview_all_certificates_title)
                    ),
                    DetailItem.Infobox(
                        getString(R.string.certificates_overview_all_certificates_app_reference_title),
                        getString(R.string.certificates_overview_all_certificates_app_reference_text)
                    )
                )
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
                                groupedDgcEntry.doseNumber, groupedDgcEntry.totalSerialDoses
                            ),
                            date = getString(
                                R.string.certificates_overview_vaccination_certificate_date,
                                groupedDgcEntry.occurrence?.formatDate()
                            ),
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = it.status
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
                                        R.string.certificates_overview_pcr_test_certificate_date,
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = it.status
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
                                        groupedDgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime()
                                    ),
                                    isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                                    certStatus = it.status
                                )
                            }
                            TestCertType.POSITIVE_PCR_TEST, TestCertType.POSITIVE_ANTIGEN_TEST -> null
                        }
                    }
                    is Recovery -> {
                        val date = if (groupedDgcEntry.validFrom.isInFuture()) {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_from_date,
                                groupedDgcEntry.validFrom?.formatDateOrEmpty()
                            )
                        } else {
                            getString(
                                R.string.certificates_overview_recovery_certificate_valid_until_date,
                                groupedDgcEntry.validUntil?.formatDateOrEmpty()
                            )
                        }
                        DetailItem.Certificate(
                            id = groupedDgcEntry.id,
                            type = groupedDgcEntry.type,
                            title = getString(R.string.certificates_overview_recovery_certificate_title),
                            subtitle = getString(R.string.certificates_overview_recovery_certificate_message),
                            date = date,
                            isActual = mainCertificate.covCertificate.dgcEntry.id == groupedDgcEntry.id,
                            certStatus = it.status
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

    override fun onShowCertificateClicked() {
        val certList = covpassDeps.certRepository.certs.value
        val groupedCertificate = certList.getGroupedCertificates(args.certId) ?: return
        val mainCertificate = groupedCertificate.getMainCertificate()
        findNavigator().push(DisplayQrCodeFragmentNav(mainCertificate.covCertificate.dgcEntry.id))
    }

    override fun onNewCertificateScanClicked() {
        findNavigator().push(AddCovCertificateFragmentNav())
    }

    override fun onCovCertificateClicked(id: String, dgcEntryType: DGCEntryType) {
        when (dgcEntryType) {
            VaccinationCertType.VACCINATION_INCOMPLETE,
            VaccinationCertType.VACCINATION_COMPLETE,
            VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                findNavigator().push(VaccinationDetailFragmentNav(id))
            }
            TestCertType.NEGATIVE_PCR_TEST,
            TestCertType.NEGATIVE_ANTIGEN_TEST -> {
                findNavigator().push(TestDetailFragmentNav(id))
            }
            RecoveryCertType.RECOVERY -> {
                findNavigator().push(RecoveryDetailFragmentNav(id))
            }
            TestCertType.POSITIVE_PCR_TEST,
            TestCertType.POSITIVE_ANTIGEN_TEST -> return
            // .let{} to enforce exhaustiveness
        }.let {}
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
        val manager = context?.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
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
        updateViews(covpassDeps.certRepository.certs.value)
    }
}
