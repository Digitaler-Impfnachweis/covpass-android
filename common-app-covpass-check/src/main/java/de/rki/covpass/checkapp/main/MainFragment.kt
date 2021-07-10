/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.main

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.CovpassCheckMainBinding
import de.rki.covpass.checkapp.information.CovPassCheckInformationFragmentNav
import de.rki.covpass.checkapp.scanner.*
import de.rki.covpass.checkapp.scanner.CameraDisclosureFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckHardwareScannerFragmentNav
import de.rki.covpass.checkapp.scanner.CovPassCheckQRScannerFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted
import de.rki.covpass.commonapp.utils.isWriteExternalStoragePermissionGranted
import de.rki.covpass.sdk.storage.DscRepository
import de.rki.covpass.sdk.dependencies.sdkDeps
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.isDscListUpToDate
import kotlinx.parcelize.Parcelize
import java.io.IOException
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

    private lateinit var dataWedgeHelper: DataWedgeHelper

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                try {
                    if (dataWedgeHelper.isInstalled) {
                        dataWedgeHelper.install()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private val hardwareScanner = HardwareScanner(object : ScanReceiver {
        override fun scanResult(result: String) {
            findNavigator().push(CovPassCheckHardwareScannerFragmentNav(result))
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener {
            findNavigator().push(CovPassCheckInformationFragmentNav())
        }
        binding.mainCheckCertButton.setOnClickListener {
            if (isCameraPermissionGranted(requireContext())) {
                findNavigator().push(CovPassCheckQRScannerFragmentNav())
            } else {
                findNavigator().push(CameraDisclosureFragmentNav())
            }
        }

        binding.hasHardwareScanner = hasHardwareScanner()
        dataWedgeHelper = DataWedgeHelper(requireContext())
        if (dataWedgeHelper.isInstalled) {
            if (isWriteExternalStoragePermissionGranted(requireContext())) {
                try {
                    dataWedgeHelper.install()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        autoRun {
            updateAvailabilityCard(get(sdkDeps.dscRepository.lastUpdate))
        }
    }

    override fun onResume() {
        super.onResume()
        this.context?.let { hardwareScanner.start(it) }
    }

    override fun onPause() {
        super.onPause()
        this.context?.let { hardwareScanner.stop(it) }
    }

    private fun updateAvailabilityCard(lastUpdate: Instant) {
        val upToDate = isDscListUpToDate(lastUpdate)

        val availabilityStatusIconId = if (upToDate) {
            R.drawable.availability_success
        } else {
            R.drawable.availability_warning
        }
        binding.mainAvailabilityStatusImageview.setImageResource(availabilityStatusIconId)

        val availabilityStatusString = if (upToDate) {
            getString(R.string.validation_start_screen_offline_modus_note_latest_version)
        } else {
            getString(R.string.validation_start_screen_offline_modus_note_old_version)
        }
        binding.mainAvailabilityStatusTextview.text = availabilityStatusString

        if (lastUpdate == DscRepository.NO_UPDATE_YET) {
            binding.mainAvailabilityLastUpdateTextview.isGone = true
        } else {
            binding.mainAvailabilityLastUpdateTextview.isGone = false
            binding.mainAvailabilityLastUpdateTextview.text = getString(
                R.string.validation_start_screen_offline_modus_note_update_pattern,
                LocalDateTime.ofInstant(lastUpdate, ZoneId.systemDefault()).formatDateTime()
            )
        }
    }
}
