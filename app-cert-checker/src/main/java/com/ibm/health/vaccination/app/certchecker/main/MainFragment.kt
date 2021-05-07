package com.ibm.health.vaccination.app.certchecker.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.dependencies.commonDeps
import com.ibm.health.common.vaccination.app.information.InformationFragmentNav
import com.ibm.health.common.vaccination.app.storage.DscRepository
import com.ibm.health.common.vaccination.app.utils.formatDateTime
import com.ibm.health.vaccination.app.certchecker.R
import com.ibm.health.vaccination.app.certchecker.databinding.CheckerMainBinding
import com.ibm.health.vaccination.app.certchecker.scanner.ValidationQRScannerFragmentNav
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(CheckerMainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(InformationFragmentNav()) }
        binding.mainCheckCertButton.setOnClickListener { findNavigator().push(ValidationQRScannerFragmentNav()) }

        autoRun {
            updateAvailabilityCard(get(commonDeps.dscRepository.lastUpdate))
        }
    }

    private fun updateAvailabilityCard(lastUpdate: LocalDateTime) {

        // TODO handle status text and icon correctly when the feature is implemented

        val updateString = getString(R.string.main_availability_last_update, lastUpdate.formatDateTime())
        binding.mainAvailabilityLastUpdateTextview.text = updateString

        binding.mainAvailabilityLastUpdateTextview.isInvisible = lastUpdate == DscRepository.NO_UPDATE_YET
    }
}
