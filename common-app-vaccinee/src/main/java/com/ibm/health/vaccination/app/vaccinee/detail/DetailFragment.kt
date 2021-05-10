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
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.dialog.DialogAction
import com.ibm.health.common.vaccination.app.dialog.DialogListener
import com.ibm.health.common.vaccination.app.dialog.DialogModel
import com.ibm.health.common.vaccination.app.dialog.showDialog
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.add.AddVaccinationCertificateFragmentNav
import com.ibm.health.vaccination.app.vaccinee.databinding.DetailBinding
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.utils.formatDate
import com.ibm.health.vaccination.sdk.android.utils.formatDateOrEmpty
import kotlinx.parcelize.Parcelize

internal interface DetailCallback {
    fun onDeletionCompleted()
    fun onShowCertClick(certId: String)
}

@Parcelize
internal class DetailFragmentNav(val certId: String) : FragmentNav(DetailFragment::class)

internal class DetailFragment : BaseFragment(), DetailEvents, DialogListener {

    private val args: DetailFragmentNav by lazy { getArgs() }
    private val state by buildState { DetailState(scope, args.certId) }
    private val binding by viewBinding(DetailBinding::inflate)
    private var isFavorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun { updateViews(get(vaccineeDeps.certRepository.certs)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val favoriteItem = menu.add(Menu.NONE, FAVORITE_ITEM_ID, Menu.NONE, R.string.vaccination_favorite_button_hint)
        val favoriteIcon = if (isFavorite) R.drawable.star_black_fill else R.drawable.star_black
        favoriteItem.setIcon(favoriteIcon)
        favoriteItem.setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == FAVORITE_ITEM_ID) {
            state.onFavoriteClick(args.certId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onDeleteDone() {
        findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
    }

    private fun updateViews(certList: GroupedCertificatesList) {
        val certId = args.certId
        // Can be null after deletion... in this case no update is necessary anymore
        val groupedCertificate = certList.getGroupedCertificates(certId) ?: return
        val mainCertificate = groupedCertificate.getMainCertificate()
        val isComplete = groupedCertificate.isComplete()
        isFavorite = certList.isMarkedAsFavorite(certId)
        setHasOptionsMenu(certList.certificates.size > 1)
        activity?.invalidateOptionsMenu()
        mainCertificate.vaccinationCertificate.let { cert ->
            binding.detailNameTextview.text = cert.fullName

            val statusHeaderText = if (isComplete) {
                getString(R.string.vaccination_certificate_detail_view_complete_title)
            } else {
                getString(
                    R.string.vaccination_certificate_detail_view_incomplete_title,
                    cert.currentSeries.toString(),
                    cert.completeSeries.toString()
                )
            }
            binding.detailStatusHeaderTextview.text = statusHeaderText

            val statusTextRes = if (isComplete) {
                R.string.vaccination_certificate_detail_view_complete_message
            } else {
                R.string.vaccination_certificate_detail_view_incomplete_message
            }
            binding.detailStatusTextview.setText(statusTextRes)

            val statusIconRes = if (isComplete) {
                R.drawable.detail_vaccination_status_complete
            } else {
                R.drawable.detail_vaccination_status_incomplete
            }
            binding.detailStatusImageview.setImageResource(statusIconRes)

            val proofButtonRes = if (isComplete) {
                R.string.vaccination_certificate_detail_view_complete_action_button_title
            } else {
                // TODO this is only temporary, change back again when feature shall be activated
                // R.string.detail_show_proof_button_text_incomplete
                R.string.vaccination_certificate_detail_view_incomplete_action_button_title
            }
            binding.detailShowProofButton.setText(proofButtonRes)

            binding.detailShowProofButton.setOnClickListener {
                if (isComplete) {
                    findNavigator().popUntil<DetailCallback>()?.onShowCertClick(certId)
                } else {
                    // TODO this is only temporary, change back again when feature shall be activated
                    findNavigator().push(AddVaccinationCertificateFragmentNav())
                }
            }

            binding.detailAddButton.setText(R.string.vaccination_certificate_detail_view_incomplete_action_button_title)
            binding.detailAddButton.isVisible = !isComplete

            // TODO this is only temporary, make it visible again when feature shall be activated
            binding.detailAddButton.isVisible = false

            binding.detailNameDataRow.detailDataHeaderTextview.setText(
                R.string.vaccination_certificate_detail_view_name
            )
            binding.detailNameDataRow.detailDataTextview.text = cert.fullName

            binding.detailBirthdateDataRow.detailDataHeaderTextview.setText(
                R.string.vaccination_certificate_detail_view_birthdate
            )
            binding.detailBirthdateDataRow.detailDataTextview.text = cert.birthDate.formatDateOrEmpty()

            binding.detailVaccinationContainer.removeAllViews()

            groupedCertificate.incompleteCertificate?.let { addVaccinationView(it.vaccinationCertificate) }
            groupedCertificate.completeCertificate?.let { addVaccinationView(it.vaccinationCertificate) }
        }

        binding.detailDeleteButton.setOnClickListener {
            val dialogModel = DialogModel(
                titleRes = R.string.dialog_delete_certificate_title,
                titleParameter = mainCertificate.vaccinationCertificate.fullName,
                messageRes = R.string.dialog_delete_certificate_message,
                positiveButtonTextRes = R.string.dialog_delete_certificate_button_delete,
                negativeButtonTextRes = R.string.dialog_delete_certificate_button_cancel,
                positiveActionColorRes = R.color.danger,
                tag = DELETE_DIALOG_TAG,
            )
            showDialog(dialogModel, childFragmentManager)
        }
    }

    private fun addVaccinationView(cert: VaccinationCertificate) {
        if (cert.vaccinations.isEmpty()) {
            return
        }
        val vaccination = cert.vaccination

        val vaccinationView = layoutInflater.inflate(
            R.layout.detail_vaccination_view,
            binding.detailVaccinationContainer,
            false
        ) as LinearLayout

        val headerText = getString(
            R.string.vaccination_certificate_detail_view_vaccination_title,
            cert.currentSeries.toString(),
            cert.completeSeries.toString()
        )
        vaccinationView.findViewById<TextView>(R.id.detail_vaccination_header_textview).text = headerText

        val occurrenceRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_occurrence_data_row)
        val occurrenceDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_occurrence_data_divider)
        findRowHeaderView(occurrenceRow).setText(R.string.vaccination_certificate_detail_view_data_date)
        findRowTextView(occurrenceRow).text = vaccination.occurrence?.formatDate()
        occurrenceRow.isVisible = !vaccination.occurrence?.formatDate().isNullOrBlank()
        occurrenceDivider.isVisible = !vaccination.occurrence?.formatDate().isNullOrBlank()

        val productRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_product_data_row)
        val productDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_product_data_divider)
        findRowHeaderView(productRow).setText(R.string.vaccination_certificate_detail_view_data_vaccine)
        findRowTextView(productRow).text = vaccination.product
        productRow.isVisible = vaccination.product.isNotBlank()
        productDivider.isVisible = vaccination.product.isNotBlank()

        val manufacturerRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_manufacturer_data_row)
        val manufacturerDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_manufacturer_data_divider)
        findRowHeaderView(manufacturerRow).setText(R.string.vaccination_certificate_detail_view_data_producer)
        findRowTextView(manufacturerRow).text = vaccination.manufacturer
        manufacturerRow.isVisible = vaccination.manufacturer.isNotBlank()
        manufacturerDivider.isVisible = vaccination.manufacturer.isNotBlank()

        val lotNumberRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_lotnumber_data_row)
        val lotNumberDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_lotnumber_data_divider)
        findRowHeaderView(lotNumberRow).setText(R.string.vaccination_certificate_detail_view_data_bench_number)
        findRowTextView(lotNumberRow).text = vaccination.lotNumber
        lotNumberRow.isVisible = vaccination.lotNumber.isNotBlank()
        lotNumberDivider.isVisible = vaccination.lotNumber.isNotBlank()

        val issuerRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_issuer_data_row)
        val issuerDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_issuer_data_divider)
        findRowHeaderView(issuerRow).setText(R.string.vaccination_certificate_detail_view_data_exhibitor)
        findRowTextView(issuerRow).text = cert.issuer
        issuerRow.isVisible = cert.issuer.isNotBlank()
        issuerDivider.isVisible = cert.issuer.isNotBlank()

        val countryRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_country_data_row)
        val countryDivider = vaccinationView.findViewById<View>(R.id.detail_vaccination_country_data_divider)
        findRowHeaderView(countryRow).setText(R.string.vaccination_certificate_detail_view_data_country)
        findRowTextView(countryRow).text = vaccination.country
        countryRow.isVisible = vaccination.country.isNotBlank()
        countryDivider.isVisible = vaccination.country.isNotBlank()

        val uvciRow = vaccinationView.findViewById<LinearLayout>(R.id.detail_vaccination_uvci_data_row)
        findRowHeaderView(uvciRow).setText(R.string.vaccination_certificate_detail_view_data_identification_number)
        findRowTextView(uvciRow).text = vaccination.id
        uvciRow.isVisible = vaccination.id.isNotBlank()

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
            binding.detailToolbar.title = getString(R.string.vaccination_certificate_detail_view_title)
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
