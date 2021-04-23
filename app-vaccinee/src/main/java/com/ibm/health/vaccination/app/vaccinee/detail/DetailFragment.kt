package com.ibm.health.vaccination.app.vaccinee.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.dialog.showDialog
import com.ibm.health.common.vaccination.app.utils.getFormattedDate
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.common.ScannerResultFragment
import com.ibm.health.vaccination.app.vaccinee.databinding.DetailBinding
import com.ibm.health.vaccination.app.vaccinee.main.MainActivity
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import com.ibm.health.vaccination.app.vaccinee.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate
import kotlinx.parcelize.Parcelize

interface DetailCallback {

    fun onDeletionCompleted()
}

@Parcelize
class DetailFragmentNav(val certId: String) : FragmentNav(DetailFragment::class)

class DetailFragment : ScannerResultFragment(), DetailEvents, DialogListener {

    private val state by buildState { DetailState(scope, getArgs<DetailFragmentNav>().certId) }
    private val binding by viewBinding(DetailBinding::inflate)
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun { updateViews(get(Storage.certCache)) }
        binding.detailDeleteButton.setOnClickListener {
            val dialogModel = DialogModel(
                titleRes = R.string.detail_delete_dialog_header,
                messageRes = R.string.detail_delete_dialog_message,
                positiveButtonTextRes = R.string.detail_delete_dialog_positive,
                negativeButtonTextRes = R.string.detail_delete_dialog_negative,
                positiveActionColorRes = R.color.danger,
                tag = DELETE_DIALOG_TAG,
            )
            showDialog(dialogModel, childFragmentManager)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val favoriteItem = menu.add(Menu.NONE, FAVORITE_ITEM_ID, Menu.NONE, R.string.detail_favorite_title)
        val favoriteIcon = if (isFavorite) R.drawable.star_black_fill else R.drawable.star_black
        favoriteItem.setIcon(favoriteIcon)
        favoriteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == FAVORITE_ITEM_ID) {
            state.onFavoriteClick(getArgs<DetailFragmentNav>().certId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun handleQrContent(qrContent: String) {
        state.onQrContentReceived(qrContent)
    }

    override fun onDeleteDone() {
        findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
    }

    private fun updateViews(certList: GroupedCertificatesList) {
        val certId = getArgs<DetailFragmentNav>().certId
        val groupedCertificate = certList.getGroupedCertificates(certId)
        val mainCertificate = groupedCertificate.getMainCertificate()
        val isComplete = groupedCertificate.isComplete()
        isFavorite = certList.isMarkedAsFavorite(certId)
        setHasOptionsMenu(certList.certificates.size > 1)
        activity?.invalidateOptionsMenu()
        mainCertificate.vaccinationCertificate.let { cert ->
            binding.detailNameTextview.text = cert.name

            val statusHeaderText = if (isComplete) {
                getString(R.string.detail_status_header_complete)
            } else {
                getString(R.string.detail_status_header_incomplete, cert.getCurrentSeries(), cert.getCompleteSeries())
            }
            binding.detailStatusHeaderTextview.text = statusHeaderText

            val statusTextRes = if (isComplete) {
                R.string.detail_status_text_complete
            } else {
                R.string.detail_status_text_incomplete
            }
            binding.detailStatusTextview.setText(statusTextRes)

            val statusIconRes = if (isComplete) {
                R.drawable.detail_vaccination_status_complete
            } else {
                R.drawable.detail_vaccination_status_incomplete
            }
            binding.detailStatusImageview.setImageResource(statusIconRes)

            val proofButtonRes = if (isComplete) {
                R.string.detail_show_proof_button_text_complete
            } else {
                // TODO this is only temporary, change back again when feature shall be activated
                // R.string.detail_show_proof_button_text_incomplete
                R.string.detail_add_button_text
            }
            binding.detailShowProofButton.setText(proofButtonRes)

            binding.detailShowProofButton.setOnClickListener {
                if (isComplete) {
                    findNavigator().pop()
                } else {
                    // TODO this is only temporary, change back again when feature shall be activated
                    (requireActivity() as? MainActivity)?.launchScanner()
                }
            }

            binding.detailAddButton.setText(R.string.detail_add_button_text)
            binding.detailAddButton.isVisible = !isComplete

            // TODO this is only temporary, make it visible again when feature shall be activated
            binding.detailAddButton.isVisible = false

            binding.detailNameDataRow.detailDataHeaderTextview.setText(R.string.detail_name_header)
            binding.detailNameDataRow.detailDataTextview.text = cert.name

            binding.detailBirthdateDataRow.detailDataHeaderTextview.setText(R.string.detail_birthdate_header)
            binding.detailBirthdateDataRow.detailDataTextview.text = cert.getFormattedBirthDate()

            binding.detailVaccinationContainer.removeAllViews()

            groupedCertificate.incompleteCertificate?.let { addVaccinationView(it.vaccinationCertificate) }
            groupedCertificate.completeCertificate?.let { addVaccinationView(it.vaccinationCertificate) }
        }
    }

    private fun addVaccinationView(cert: VaccinationCertificate) {
        if (cert.vaccination.isEmpty()) {
            return
        }
        val vaccination = cert.vaccination.first()

        val vaccinationView = layoutInflater.inflate(
            R.layout.detail_vaccination_view,
            binding.detailVaccinationContainer,
            false
        ) as LinearLayout

        val headerText = getString(
            R.string.detail_vaccination_header,
            cert.getCurrentSeries(),
            cert.getCompleteSeries()
        )
        vaccinationView.findViewById<TextView>(R.id.detail_vaccination_header_textview).text = headerText

        val occurrenceRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_occurrence_data_row)
        findRowHeaderView(occurrenceRow).setText(R.string.detail_vaccination_occurrence)
        findRowTextView(occurrenceRow).text = vaccination.occurence?.getFormattedDate()
        occurrenceRow.isVisible = !vaccination.occurence?.getFormattedDate().isNullOrBlank()

        val productRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_product_data_row)
        findRowHeaderView(productRow).setText(R.string.detail_vaccination_product)
        findRowTextView(productRow).text = vaccination.product
        productRow.isVisible = vaccination.product.isNotBlank()

        val manufacturerRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_manufacturer_data_row)
        findRowHeaderView(manufacturerRow).setText(R.string.detail_vaccination_manufacturer)
        findRowTextView(manufacturerRow).text = vaccination.manufacturer
        manufacturerRow.isVisible = vaccination.manufacturer.isNotBlank()

        val lotNumberRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_lotnumber_data_row)
        findRowHeaderView(lotNumberRow).setText(R.string.detail_vaccination_lotnumber)
        findRowTextView(lotNumberRow).text = vaccination.lotNumber
        lotNumberRow.isVisible = vaccination.lotNumber.isNotBlank()

        val locationRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_location_data_row)
        findRowHeaderView(locationRow).setText(R.string.detail_vaccination_location)
        findRowTextView(locationRow).text = vaccination.location
        locationRow.isVisible = vaccination.location.isNotBlank()

        val issuerRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_issuer_data_row)
        findRowHeaderView(issuerRow).setText(R.string.detail_vaccination_issuer)
        findRowTextView(issuerRow).text = cert.issuer
        issuerRow.isVisible = cert.issuer.isNotBlank()

        val countryRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_country_data_row)
        findRowHeaderView(countryRow).setText(R.string.detail_vaccination_country)
        findRowTextView(countryRow).text = vaccination.country
        countryRow.isVisible = vaccination.country.isNotBlank()

        val uvciRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_uvci_data_row)
        findRowHeaderView(uvciRow).setText(R.string.detail_vaccination_uvci)
        findRowTextView(uvciRow).text = cert.id
        uvciRow.isVisible = cert.id.isNotBlank()

        binding.detailVaccinationContainer.addView(vaccinationView)
    }

    private fun findRowHeaderView(dataRow: LinearLayout) =
        dataRow.findViewById<TextView>(R.id.detail_data_header_textview)

    private fun findRowTextView(dataRow: LinearLayout) =
        dataRow.findViewById<TextView>(R.id.detail_data_textview)

    private fun setupActionBar() {
        val activity = (activity as? AppCompatActivity)
        activity?.run {
            setSupportActionBar(binding.detailToolbar)
            supportActionBar?.run {
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(true)
                val icon = R.drawable.back_arrow
                setHomeAsUpIndicator(icon)
            }
            binding.detailToolbar.title = getString(R.string.covid_header)
        }
    }

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == DELETE_DIALOG_TAG && action == DialogAction.POSITIVE) {
            state.onDelete()
        }
    }

    private companion object {
        private const val FAVORITE_ITEM_ID = 82957
        private const val DELETE_DIALOG_TAG = "delete_dialog"
    }
}
