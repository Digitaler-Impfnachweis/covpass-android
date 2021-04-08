package com.ibm.health.vaccination.app.detail

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.vaccination.app.databinding.DetailBinding
import com.ibm.health.vaccination.app.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.decode.models.VaccinationCertificate
import kotlinx.parcelize.Parcelize
import java.lang.IllegalStateException

@Parcelize
class DetailFragmentNav : FragmentNav(DetailFragment::class)

class DetailFragment : BaseFragment() {

    private val binding by viewBinding(DetailBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // FIXME this is just a provisionally implementation
        Storage.getVaccinationCertificate()?.let {
            binding.detailTextview.text =
                "${it.name}\n" +
                "Geboren am ${it.birthDate}\n" +
                "Ausweisnummer ${it.identifier}\n" +
                "Ausstellungsdatum ${it.validFrom}\n" +
                "${generateVaccinationStrings(it)}"
        } ?: run {
            throw IllegalStateException()
        }
    }

    private fun generateVaccinationStrings(vaccinationCertificate: VaccinationCertificate): String =
        vaccinationCertificate.vaccination.map {
            "Impfung ${it.series}\n" +
                "${it.location}\n" +
                "${it.occurence}\n" +
                "Impfstoff ${it.product}\n" +
                "Chargennummer ${it.lotNumber}\n"
        }.joinToString("")
}
