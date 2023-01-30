/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.boosterreissue.ReissueNotificationFragmentNav
import de.rki.covpass.app.databinding.DgcEntryDetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.adapter.DgcEntryDetailAdapter
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.stripUnderlinesAndSetExternalLinkImage
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.ReissueState
import de.rki.covpass.sdk.cert.models.ReissueType
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.rules.CovPassValueSetsRepository
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatTimeOrEmpty
import java.util.Locale

/**
 * Interface to communicate events from [DgcEntryDetailFragment] back to other fragments.
 */
internal interface DgcEntryDetailCallback {
    fun onDeletionCompleted(isGroupedCertDeleted: Boolean)
}

/**
 * Base fragment for displaying the details of a [Vaccination], [TestCert] or [Recovery].
 */
public abstract class DgcEntryDetailFragment : BaseFragment(), DgcEntryDetailEvents, DialogListener {

    protected abstract val certId: String
    protected val valueSetsRepository: CovPassValueSetsRepository by lazy { sdkDeps.covPassValueSetsRepository }

    private val viewModel by reactiveState { DgcEntryDetailViewModel(scope) }
    private val binding by viewBinding(DgcEntryDetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        launchWhenStarted { viewModel.checkPdfExport(certId) }
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
        autoRun { updatePdfButton(get(viewModel.isPdfExportEnabled), get(viewModel.isCertificateRevoked)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val deleteItem = menu.add(
            Menu.NONE,
            DELETE_ITEM_ID,
            Menu.NONE,
            getString(R.string.accessibility_certificate_detail_view_label_delete_button),
        )
        deleteItem.setIcon(R.drawable.trash)
        deleteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onBackPressed(): Abortable {
        requireContext().cacheDir.deleteRecursively()
        return super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == DELETE_ITEM_ID) {
            val dialogModel = DialogModel(
                titleRes = R.string.dialog_delete_certificate_title,
                messageString = getString(R.string.dialog_delete_certificate_message),
                positiveButtonTextRes = R.string.dialog_delete_certificate_button_delete,
                negativeButtonTextRes = R.string.dialog_delete_certificate_button_cancel,
                positiveActionColorRes = R.color.danger,
                tag = DELETE_DIALOG_TAG,
            )
            showDialog(dialogModel, childFragmentManager)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == DELETE_DIALOG_TAG && action == DialogAction.POSITIVE) {
            viewModel.onDelete(certId)
        }
    }

    override fun onDeleteDone(isGroupedCertDeleted: Boolean) {
        requireContext().cacheDir.deleteRecursively()
        findNavigator().popUntil<DgcEntryDetailCallback>()?.onDeletionCompleted(isGroupedCertDeleted)
    }

    public abstract fun getToolbarTitleText(cert: CovCertificate): String

    public abstract fun getHeaderText(): String

    public abstract fun getHeaderAccessibleText(): String

    public abstract fun getDataRows(cert: CovCertificate): List<DataRow>

    private fun startRecyclerView(cert: CovCertificate) {
        DgcEntryDetailAdapter(this).apply {
            updateList(
                getDataRows(cert).filterNot {
                    it.value.isNullOrEmpty()
                },
            )
            attachTo(binding.dgcDetailRecyclerView)
        }
    }

    private fun updateViews(certs: GroupedCertificatesList) {
        val combinedCovCertificate = certs.getCombinedCertificate(certId) ?: return
        val covCertificate = combinedCovCertificate.covCertificate
        val groupedCertificate = certs.certificates.find {
            it.certificates.any { combCert ->
                combCert.covCertificate.dgcEntry.id == certId
            }
        } ?: return
        setupActionBar(covCertificate)
        binding.dgcDetailHeaderTextview.text = getHeaderText()
        binding.dgcDetailHeaderTextview.contentDescription = getHeaderAccessibleText()
        showExpirationInfoElement(combinedCovCertificate, groupedCertificate)

        startRecyclerView(covCertificate)

        binding.dgcDetailDisplayQrButton.setOnClickListener {
            findNavigator().push(DisplayQrCodeFragmentNav(certId))
        }
        binding.dgcDetailExportPdfButton.setOnClickListener {
            findNavigator().push(DetailExportPdfFragmentNav(certId))
        }

        binding.dgcDetailInfoFooterGerman.apply {
            text = getSpanned(R.string.recovery_certificate_detail_view_data_test_note_de)
            textLocale = Locale.GERMAN
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlinesAndSetExternalLinkImage()
        }
        binding.dgcDetailInfoFooterEnglish.apply {
            text = getSpanned(R.string.recovery_certificate_detail_view_data_test_note_en)
            textLocale = Locale.ENGLISH
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlinesAndSetExternalLinkImage()
        }
        if (Locale.getDefault() == Locale.GERMANY) {
            binding.dgcDetailInfoFooterEnglish.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        } else {
            binding.dgcDetailInfoFooterGerman.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }

    private fun showExpirationInfoElement(
        combinedCovCertificate: CombinedCovCertificate,
        groupedCertificate: GroupedCertificates,
    ) {
        binding.dgcDetailExpirationInfoElement.isVisible = true
        binding.dgcDetailReissueLayout.isVisible = true
        val isVaccination = combinedCovCertificate.covCertificate.dgcEntry is Vaccination
        when (combinedCovCertificate.status) {
            CertValidationResult.ExpiryPeriod -> {
                if (
                    combinedCovCertificate.reissueState == ReissueState.Completed ||
                    combinedCovCertificate.reissueState == ReissueState.None
                ) {
                    binding.dgcDetailExpirationInfoElement.showInfo(
                        title = getString(
                            R.string.certificate_expires_detail_view_note_title,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        ),
                        descriptionNoLink = getString(
                            if (combinedCovCertificate.covCertificate.isGermanCertificate) {
                                R.string.certificate_expires_detail_view_note_message
                            } else {
                                R.string.certificate_expires_detail_view_note_nonDE
                            },
                        ),
                        iconRes = R.drawable.main_cert_expiry_period,
                    )
                    binding.dgcDetailReissueLayout.isGone = true
                } else {
                    if (isVaccination) {
                        reissueVaccination(combinedCovCertificate, groupedCertificate)
                    } else {
                        reissueRecovery(combinedCovCertificate, groupedCertificate)
                    }
                    binding.dgcDetailExpirationInfoElement.isGone = true
                }
            }
            CertValidationResult.Expired -> {
                when (combinedCovCertificate.reissueState) {
                    ReissueState.Completed, ReissueState.None -> {
                        binding.dgcDetailExpirationInfoElement.showWarning(
                            title = getString(R.string.certificate_expired_detail_view_note_title),
                            descriptionNoLink = if (combinedCovCertificate.covCertificate.isGermanCertificate) {
                                getString(
                                    R.string.certificate_expired_detail_view_note_message,
                                    combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                                    combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                                )
                            } else {
                                getString(R.string.certificate_expires_detail_view_note_nonDE)
                            },
                            iconRes = R.drawable.info_warning_icon,
                        )
                        binding.dgcDetailReissueLayout.isGone = true
                    }
                    else -> {
                        if (isVaccination) {
                            reissueVaccination(combinedCovCertificate, groupedCertificate)
                        } else {
                            reissueRecovery(combinedCovCertificate, groupedCertificate)
                        }
                        binding.dgcDetailExpirationInfoElement.isGone = true
                    }
                }
            }
            CertValidationResult.Revoked -> {
                binding.dgcDetailExpirationInfoElement.showWarning(
                    title = getString(R.string.certificates_overview_invalid_title),
                    descriptionNoLink = getString(
                        if (combinedCovCertificate.covCertificate.isGermanCertificate) {
                            R.string.revocation_detail_single_DE
                        } else {
                            R.string.revocation_detail_single_notDE
                        },
                    ),
                    iconRes = R.drawable.info_warning_icon,
                )
                binding.dgcDetailReissueLayout.isGone = true
            }
            CertValidationResult.Invalid -> {
                binding.dgcDetailExpirationInfoElement.showWarning(
                    title = getString(R.string.certificate_invalid_detail_view_note_title),
                    descriptionNoLink = getString(R.string.certificate_invalid_detail_view_note_message),
                    iconRes = R.drawable.info_warning_icon,
                )
                binding.dgcDetailReissueLayout.isGone = true
            }
            else -> {
                binding.dgcDetailExpirationInfoElement.isGone = true
                binding.dgcDetailReissueLayout.isGone = true
            }
        }
    }

    private fun reissueVaccination(
        combinedCovCertificate: CombinedCovCertificate,
        groupedCertificate: GroupedCertificates,
    ) {
        updateReissueElement(
            when (combinedCovCertificate.reissueState) {
                ReissueState.Ready -> {
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        R.string.renewal_bluebox_title_expired_vaccination
                    } else {
                        R.string.renewal_bluebox_title_expiring_soon_vaccination
                    }
                }
                ReissueState.AfterTimeLimit, ReissueState.NotGermanReady -> {
                    R.string.certificates_overview_expired_certificate_note
                }
                else ->
                    R.string.renewal_bluebox_title_expiring_soon_vaccination
            },
            when (combinedCovCertificate.reissueState) {
                ReissueState.Ready -> {
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        getString(
                            R.string.renewal_bluebox_copy_expired,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    } else {
                        getString(
                            R.string.renewal_bluebox_copy_expiring_soon,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    }
                }
                ReissueState.NotGermanReady ->
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        getString(R.string.renewal_bluebox_copy_expiry_not_german)
                    } else {
                        getString(
                            R.string.renewal_bluebox_copy_expiring_soon_not_german,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    }
                else ->
                    getString(
                        R.string.renewal_bluebox_copy_expiry_not_available,
                        combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                        combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                    )
            },
            R.string.renewal_expiry_notification_button_vaccination,
            combinedCovCertificate.reissueState == ReissueState.Ready,
        ) {
            findNavigator().push(
                ReissueNotificationFragmentNav(
                    ReissueType.Vaccination,
                    groupedCertificate.getListOfVaccinationIdsReadyForReissue(),
                ),
            )
        }
    }

    private fun reissueRecovery(
        combinedCovCertificate: CombinedCovCertificate,
        groupedCertificate: GroupedCertificates,
    ) {
        updateReissueElement(
            when (combinedCovCertificate.reissueState) {
                ReissueState.Ready -> {
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        R.string.renewal_bluebox_title_expired_recovery
                    } else {
                        R.string.renewal_bluebox_title_expiring_soon_recovery
                    }
                }
                else ->
                    R.string.renewal_bluebox_title_expiring_soon_recovery
            },
            when (combinedCovCertificate.reissueState) {
                ReissueState.Ready -> {
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        getString(
                            R.string.renewal_bluebox_copy_expired,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    } else {
                        getString(
                            R.string.renewal_bluebox_copy_expiring_soon,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    }
                }
                ReissueState.NotGermanReady ->
                    if (combinedCovCertificate.status == CertValidationResult.Expired) {
                        getString(R.string.renewal_bluebox_copy_expiry_not_german)
                    } else {
                        getString(
                            R.string.renewal_bluebox_copy_expiring_soon_not_german,
                            combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                            combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                        )
                    }
                else ->
                    getString(
                        R.string.renewal_bluebox_copy_expiry_not_available,
                        combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                        combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                    )
            },
            R.string.renewal_expiry_notification_button_recovery,
            combinedCovCertificate.reissueState == ReissueState.Ready,
        ) {
            findNavigator().push(
                ReissueNotificationFragmentNav(
                    ReissueType.Recovery,
                    listOf(certId) + groupedCertificate.getHistoricalDataForDcc(certId),
                ),
            )
        }
    }

    private fun updateReissueElement(
        titleRes: Int,
        text: String,
        buttonRes: Int,
        isButtonVisible: Boolean,
        buttonClickListener: View.OnClickListener?,
    ) {
        binding.dgcDetailReissueElement.reissueNotificationTitle.text = getString(titleRes)
        binding.dgcDetailReissueElement.reissueNotificationText.text = text
        binding.dgcDetailReissueElement.reissueNotificationButton.setText(buttonRes)
        binding.dgcDetailReissueElement.reissueNotificationButton.isVisible = isButtonVisible
        binding.dgcDetailReissueElement.reissueNotificationButton.setOnClickListener(buttonClickListener)
    }

    private fun updatePdfButton(isEnabled: Boolean, isRevoked: Boolean) {
        binding.dgcDetailExportPdfButton.isEnabled = isEnabled
        binding.dgcDetailExportPdfInfo.isGone = isEnabled
        binding.dgcDetailButtonsContainer.isGone = isRevoked
    }

    private fun setupActionBar(cert: CovCertificate) {
        attachToolbar(binding.dgcDetailToolbar)
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow_big
                setHomeAsUpIndicator(icon)
                setHomeActionContentDescription(R.string.accessibility_certificate_detail_view_label_back)
            }
            binding.dgcDetailToolbar.title = getToolbarTitleText(cert)
            binding.dgcDetailToolbar.getChildAt(1).foreground =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_highlight_selector, null)
        }
    }

    private companion object {
        private const val DELETE_ITEM_ID = 48023
        private const val DELETE_DIALOG_TAG = "delete_dialog"
    }

    public data class DataRow(
        val header: String,
        val headerAccessibleDescription: String,
        val value: String? = "",
        val description: String? = null,
        val valueAccessibleDescription: String? = null,
    )
}
