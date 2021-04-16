package com.ibm.health.vaccination.app.detail

import android.os.Bundle
import android.view.View
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
import com.ibm.health.vaccination.app.R
import com.ibm.health.vaccination.app.databinding.DetailBinding
import com.ibm.health.vaccination.app.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificateList
import kotlinx.parcelize.Parcelize

interface DetailCallback {

    fun onDeletionCompleted()
}

@Parcelize
class DetailFragmentNav(val certId: String) : FragmentNav(DetailFragment::class)

class DetailFragment : BaseFragment(), DetailEvents, DialogListener {

    private val state by buildState { DetailState(scope, getArgs<DetailFragmentNav>().certId) }
    private val binding by viewBinding(DetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // FIXME this is just a provisionally implementation
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

    private fun updateViews(certList: VaccinationCertificateList) {
        val certId = getArgs<DetailFragmentNav>().certId
        val extendedCertificate = certList.getExtendedVaccinationCertificate(certId)
        extendedCertificate.vaccinationCertificate.let {
            binding.detailTextview.text =
                "${it.name}\n" +
                "Geboren am ${it.birthDate}\n" +
                "Ausweisnummer ${it.identifier}\n" +
                "Ausstellungsdatum ${it.validFrom}\n" +
                "${generateVaccinationStrings(it)}"
        }
    }

    override fun onDeleteDone() {
        findNavigator().popUntil<DetailCallback>()?.onDeletionCompleted()
    }

    private fun generateVaccinationStrings(vaccinationCertificate: VaccinationCertificate): String =
        vaccinationCertificate.vaccination.map {
            "Impfung ${it.series}\n" +
                "${it.location}\n" +
                "${it.occurence}\n" +
                "Impfstoff ${it.product}\n" +
                "Chargennummer ${it.lotNumber}\n"
        }.joinToString("")

    override fun onDialogAction(tag: String, action: DialogAction) {
        if (tag == DELETE_DIALOG_TAG && action == DialogAction.POSITIVE) {
            state.onDelete()
        }
    }

    private companion object {
        private const val DELETE_DIALOG_TAG = "delete_dialog"
    }
}
