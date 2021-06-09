/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.attachToolbar
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.annotations.Abort
import com.ibm.health.common.annotations.Abortable
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.DetailBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.storage.GroupedCertificates
import de.rki.covpass.app.storage.GroupedCertificatesList
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dialog.DialogModel
import de.rki.covpass.commonapp.dialog.showDialog
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination
import kotlinx.parcelize.Parcelize

/**
 * Interface to communicate events from [DetailFragment] back to other fragments..
 */
internal interface DetailCallback {
    fun onDeletionCompleted()
    fun displayCert(certId: GroupedCertificatesId)
}

@Parcelize
internal class DetailFragmentNav(
    var certId: GroupedCertificatesId,
) : FragmentNav(DetailFragment::class)

/**
 * Fragment which shows the [GroupedCertificates] details
 * Further actions (Show QR Code, Add cov certificate)
 */
// FIXME BVC-1370
internal class DetailFragment : BaseFragment(), DgcEntryDetailCallback {

    private val args: DetailFragmentNav by lazy { getArgs() }
    private val viewModel by buildState { DetailViewModel(scope) }
    private val binding by viewBinding(DetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionBar()
        autoRun { updateViews(get(covpassDeps.certRepository.certs)) }
    }

    // FIXME just a mock implementation, delete or implement later
    @SuppressLint("SetTextI18n")
    private fun updateViews(certs: GroupedCertificatesList) {
        certs.getGroupedCertificates(args.certId)?.certificates?.forEach { cert ->
            val button = Button(context)
            when (cert.covCertificate.dgcEntry) {
                is Vaccination -> {
                    button.text = "Vaccination"
                    button.setOnClickListener {
                        findNavigator().push(VaccinationDetailFragmentNav(cert.covCertificate.dgcEntry.id))
                    }
                }
                is Test -> {
                    button.text = "Test"
                    button.setOnClickListener {
                        findNavigator().push(TestDetailFragmentNav(cert.covCertificate.dgcEntry.id))
                    }
                }
                is Recovery -> {
                    button.text = "Recovery"
                    button.setOnClickListener {
                        findNavigator().push(RecoveryDetailFragmentNav(cert.covCertificate.dgcEntry.id))
                    }
                }
            }
            binding.detailMainContainer.addView(button)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == FAVORITE_ITEM_ID) {
            viewModel.onFavoriteClick(args.certId)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onBackPressed(): Abortable {
        findNavigator().popUntil<DetailCallback>()?.displayCert(args.certId)
        return Abort
    }

    override fun onDeletionCompleted(isGroupedCertDeleted: Boolean) {
        if (isGroupedCertDeleted) {
            findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
        } else {
            val dialogModel = DialogModel(
                titleRes = R.string.delete_result_dialog_header,
                messageRes = R.string.delete_result_dialog_message,
                positiveButtonTextRes = R.string.delete_result_dialog_positive_button_text,
            )
            showDialog(dialogModel, childFragmentManager)
        }
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
            }
        }
    }

    private companion object {
        private const val FAVORITE_ITEM_ID = 82957
    }
}
