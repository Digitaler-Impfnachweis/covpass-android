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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import de.rki.covpass.app.databinding.DgcEntryDetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.uielements.showInfo
import de.rki.covpass.commonapp.uielements.showWarning
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatTimeOrEmpty

/**
 * Interface to communicate events from [DgcEntryDetailFragment] back to other fragments.
 */
internal interface DgcEntryDetailCallback {
    fun onDeletionCompleted(isGroupedCertDeleted: Boolean)
}

/**
 * Base fragment for displaying the details of a [Vaccination], [TestCert] or [Recovery].
 */
internal abstract class DgcEntryDetailFragment : BaseFragment(), DgcEntryDetailEvents, DialogListener {

    protected abstract val certId: String

    private val viewModel by reactiveState { DgcEntryDetailViewModel(scope) }
    private val binding by viewBinding(DgcEntryDetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        launchWhenStarted { viewModel.checkPdfExport(certId) }
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
        autoRun { updatePdfButton(get(viewModel.isPdfExportEnabled)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val deleteItem = menu.add(
            Menu.NONE, DELETE_ITEM_ID, Menu.NONE,
            getString(R.string.accessibility_certificate_detail_view_label_delete_button)
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

    abstract fun getToolbarTitleText(cert: CovCertificate): String

    abstract fun getHeaderText(): String

    abstract fun getHeaderAccessibleText(): String

    open fun isHeaderTitleVisible(cert: CovCertificate): Boolean = false

    abstract fun getDataRows(cert: CovCertificate): List<DataRow>

    abstract fun getExtendedDataRows(cert: CovCertificate): List<ExtendedDataRow>

    private fun updateViews(certs: GroupedCertificatesList) {
        val combinedCovCertificate = certs.getCombinedCertificate(certId) ?: return
        val covCertificate = combinedCovCertificate.covCertificate
        setupActionBar(covCertificate)
        binding.dgcDetailHeaderTextview.text = getHeaderText()
        binding.dgcDetailHeaderTextview.contentDescription = getHeaderAccessibleText()
        binding.dgcDetailHeaderTitleTextview.isGone = !isHeaderTitleVisible(covCertificate)
        showExpirationInfoElement(combinedCovCertificate)

        // ToDo: refactor, use Adapter s.ResultAdapter
        binding.dgcDetailDataContainer.removeAllViews()
        getDataRows(covCertificate).filterNot {
            it.value.isNullOrEmpty()
        }.forEach { dataRow ->
            val dataRowView = layoutInflater.inflate(
                R.layout.detail_data_row,
                binding.dgcDetailDataContainer,
                false
            )
            val headerTextView = dataRowView.findViewById<TextView>(R.id.detail_data_header_textview)
            val valueTextView = dataRowView.findViewById<TextView>(R.id.detail_data_textview)
            headerTextView.text = dataRow.header
            headerTextView.contentDescription = dataRow.headerAccessibleDescription
            valueTextView.text = dataRow.value

            binding.dgcDetailDataContainer.addView(dataRowView)
        }

        getExtendedDataRows(covCertificate).filterNot {
            it.value.isNullOrEmpty()
        }.forEach { extendedDataRow ->
            val extendedDataRowView = layoutInflater.inflate(
                R.layout.extended_detail_data_row,
                binding.dgcDetailDataContainer,
                false
            )
            val headerTextView =
                extendedDataRowView.findViewById<TextView>(R.id.extended_detail_data_header_textview)
            val valueTextView =
                extendedDataRowView.findViewById<TextView>(R.id.extended_detail_data_textview)
            val descriptionTextView =
                extendedDataRowView.findViewById<TextView>(R.id.extended_detail_description_textview)
            headerTextView.text = extendedDataRow.header
            headerTextView.contentDescription = extendedDataRow.headerAccessibleDescription
            valueTextView.text = extendedDataRow.value
            descriptionTextView.text = extendedDataRow.description

            binding.dgcDetailDataContainer.addView(extendedDataRowView)
        }

        binding.dgcDetailDisplayQrButton.setOnClickListener {
            findNavigator().push(DisplayQrCodeFragmentNav(certId))
        }
        binding.dgcDetailExportPdfButton.setOnClickListener {
            findNavigator().push(DetailExportPdfFragmentNav(certId))
        }

        binding.dgcDetailInfoFooterGerman.apply {
            text = getSpanned(R.string.recovery_certificate_detail_view_data_test_note_de)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.dgcDetailInfoFooterEnglish.apply {
            text = getSpanned(R.string.recovery_certificate_detail_view_data_test_note_en)
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
    }

    private fun showExpirationInfoElement(combinedCovCertificate: CombinedCovCertificate) {
        binding.dgcDetailExpirationInfoElement.isVisible = true
        when (combinedCovCertificate.status) {
            CertValidationResult.ExpiryPeriod -> {
                binding.dgcDetailExpirationInfoElement.showInfo(
                    title = getString(
                        R.string.certificate_expires_detail_view_note_title,
                        combinedCovCertificate.covCertificate.validUntil.formatDateOrEmpty(),
                        combinedCovCertificate.covCertificate.validUntil.formatTimeOrEmpty(),
                    ),
                    description = getString(R.string.certificate_expires_detail_view_note_message),
                    iconRes = R.drawable.main_cert_expiry_period
                )
            }
            CertValidationResult.Expired -> {
                binding.dgcDetailExpirationInfoElement.showWarning(
                    title = getString(R.string.certificate_expired_detail_view_note_title),
                    description = getString(R.string.certificate_expired_detail_view_note_message),
                    iconRes = R.drawable.info_warning_icon
                )
            }
            CertValidationResult.Invalid -> {
                binding.dgcDetailExpirationInfoElement.showWarning(
                    title = getString(R.string.certificate_invalid_detail_view_note_title),
                    description = getString(R.string.certificate_invalid_detail_view_note_message),
                    iconRes = R.drawable.info_warning_icon
                )
            }
            else -> binding.dgcDetailExpirationInfoElement.isGone = true
        }
    }

    private fun updatePdfButton(isEnabled: Boolean) {
        binding.dgcDetailExportPdfButton.isEnabled = isEnabled
        binding.dgcDetailExportPdfInfo.isGone = isEnabled
    }

    private fun setupActionBar(cert: CovCertificate) {
        attachToolbar(binding.dgcDetailToolbar)
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow
                setHomeAsUpIndicator(icon)
                setHomeActionContentDescription(R.string.accessibility_certificate_detail_view_label_back)
            }
            binding.dgcDetailToolbar.title = getToolbarTitleText(cert)
        }
    }

    private companion object {
        private const val DELETE_ITEM_ID = 48023
        private const val DELETE_DIALOG_TAG = "delete_dialog"
    }
}

public data class DataRow(
    val header: String,
    val headerAccessibleDescription: String,
    val value: String? = ""
)

public data class ExtendedDataRow(
    val header: String,
    val headerAccessibleDescription: String,
    val value: String? = "",
    val description: String
)
