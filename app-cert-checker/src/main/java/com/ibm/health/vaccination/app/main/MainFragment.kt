package com.ibm.health.vaccination.app.main

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.common.vaccination.app.scanner.QRScannerActivity
import com.ibm.health.vaccination.app.R
import kotlinx.parcelize.Parcelize
import com.ibm.health.vaccination.app.databinding.CheckerMainBinding
import com.ibm.health.common.vaccination.app.extensions.stripUnderlines
import java.util.*

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(CheckerMainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
        binding.mainCheckCertButton.setOnClickListener { launchScanner() }
        binding.mainFaqShowAllTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqShowAllTextview.stripUnderlines()
        binding.mainFaqCheckCertTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqCheckCertTextview.stripUnderlines()
        binding.mainFaqGetQrCodeTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqGetQrCodeTextview.stripUnderlines()

        // FIXME use correct date
        val date = Calendar.getInstance().getTime().toString()
        val updateString = String.format(resources.getString(R.string.main_availability_last_update), date)
        binding.mainAvailabilityLastUpdateTextview.text = updateString
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            if (it.contents == null) {
                Toast.makeText(requireContext(), getString(R.string.scanner_error_message), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), it.contents, Toast.LENGTH_LONG).show()
            }
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    // FIXME this will be moved to sdk later on
    private fun launchScanner() {
        IntentIntegrator(requireActivity()).run {
            captureActivity = QRScannerActivity::class.java
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
