package com.ibm.health.vaccination.app.certchecker.main

import android.os.Bundle
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.vaccination.app.certchecker.R
import com.ibm.health.vaccination.app.certchecker.databinding.CheckerMainBinding
import com.ibm.health.vaccination.app.certchecker.scanner.ValidationQRScannerActivity
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(CheckerMainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
        binding.mainCheckCertButton.setOnClickListener { launchScanner() }

        // FIXME use correct date
        val date = Calendar.getInstance().getTime().toString()
        val updateString = String.format(resources.getString(R.string.main_availability_last_update), date)
        binding.mainAvailabilityLastUpdateTextview.text = updateString
    }

    // FIXME this will be moved to sdk later on
    private fun launchScanner() {
        IntentIntegrator(requireActivity()).run {
            captureActivity = ValidationQRScannerActivity::class.java
            setDesiredBarcodeFormats(
                listOf(BarcodeFormat.QR_CODE.name, BarcodeFormat.DATA_MATRIX.name, BarcodeFormat.AZTEC.name)
            )
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
            initiateScan()
        }
    }
}
