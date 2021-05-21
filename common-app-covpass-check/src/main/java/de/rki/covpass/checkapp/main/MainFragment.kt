/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CovpassCheckMainBinding
import de.rki.covpass.checkapp.information.ValidationInformationFragmentNav
import de.rki.covpass.checkapp.scanner.ValidationQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.dependencies.commonDeps
import de.rki.covpass.commonapp.storage.DscRepository
import de.rki.covpass.sdk.android.utils.formatDateTime
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Parcelize
public class MainFragmentNav : FragmentNav(MainFragment::class)

/**
 * Displays the start view of the app.
 */
internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(CovpassCheckMainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(ValidationInformationFragmentNav()) }
        binding.mainCheckCertButton.setOnClickListener { findNavigator().push(ValidationQRScannerFragmentNav()) }

        autoRun {
            updateAvailabilityCard(get(commonDeps.dscRepository.lastUpdate))
        }
    }

    private fun updateAvailabilityCard(lastUpdate: Instant) {
        // TODO handle status text and icon correctly when the feature is implemented
        val updateString = getString(
            R.string.validation_start_screen_offline_modus_note_update_pattern,
            LocalDateTime.ofInstant(lastUpdate, ZoneId.systemDefault()).formatDateTime()
        )
        binding.mainAvailabilityLastUpdateTextview.text = updateString
        binding.mainAvailabilityLastUpdateTextview.isGone = lastUpdate == DscRepository.NO_UPDATE_YET
    }
}
