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
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DgcEntryDetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogAction
import de.rki.covpass.commonapp.dialog.DialogListener
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.commonapp.utils.stripUnderlines
import de.rki.covpass.sdk.cert.models.*

/**
 * Interface to communicate events from [DgcEntryDetailFragment] back to other fragments.
 */
internal interface DgcEntryDetailCallback {
    fun onDeletionCompleted(isGroupedCertDeleted: Boolean)
}

/**
 * Base fragment for displaying the details of a [Vaccination], [Test] or [Recovery].
 */
internal abstract class DgcEntryDetailFragment : BaseFragment(), DgcEntryDetailEvents, DialogListener {

    protected abstract val certId: String

    private val viewModel by reactiveState { DgcEntryDetailViewModel(scope) }
    private val binding by viewBinding(DgcEntryDetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val deleteItem = menu.add(
            Menu.NONE, DELETE_ITEM_ID, Menu.NONE,
            getString(R.string.certificate_delete_button_hint)
        )
        deleteItem.setIcon(R.drawable.trash)
        deleteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
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
        findNavigator().popUntil<DgcEntryDetailCallback>()?.onDeletionCompleted(isGroupedCertDeleted)
    }

    abstract fun getToolbarTitleText(cert: CovCertificate): String

    abstract fun getHeaderText(): String

    open fun isHeaderTitleVisible(cert: CovCertificate): Boolean = false

    abstract fun getDataRows(cert: CovCertificate): List<Pair<String, String>>

    protected fun addDataRow(key: String, value: String?, dataRows: MutableList<Pair<String, String>>) {
        if (!value.isNullOrEmpty()) {
            dataRows.add(Pair(key, value))
        }
    }

    private fun updateViews(certs: GroupedCertificatesList) {
        val cert = certs.getCombinedCertificate(certId)?.covCertificate ?: return
        setupActionBar(cert)
        binding.dgcDetailHeaderTextview.text = getHeaderText()
        binding.dgcDetailHeaderTitleTextview.isGone = !isHeaderTitleVisible(cert)

        binding.dgcDetailDataContainer.removeAllViews()
        getDataRows(cert).forEach {
            val dataRowView = layoutInflater.inflate(
                R.layout.detail_data_row,
                binding.dgcDetailDataContainer,
                false
            )
            val headerTextView = dataRowView.findViewById<TextView>(R.id.detail_data_header_textview)
            val valueTextView = dataRowView.findViewById<TextView>(R.id.detail_data_textview)
            headerTextView.text = it.first
            valueTextView.text = it.second

            binding.dgcDetailDataContainer.addView(dataRowView)
        }

        binding.dgcDetailDisplayQrButton.setOnClickListener {
            findNavigator().push(DisplayQrCodeFragmentNav(certId))
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

    private fun setupActionBar(cert: CovCertificate) {
        attachToolbar(binding.dgcDetailToolbar)
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow
                setHomeAsUpIndicator(icon)
            }
            binding.dgcDetailToolbar.title = getToolbarTitleText(cert)
        }
    }

    private companion object {
        private const val DELETE_ITEM_ID = 48023
        private const val DELETE_DIALOG_TAG = "delete_dialog"
    }
}
