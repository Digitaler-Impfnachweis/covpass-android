package com.ibm.health.vaccination.app.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.SimpleFragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.vaccination.app.R
import com.ibm.health.vaccination.app.databinding.MainBinding
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.parcelize.Parcelize

@Parcelize
class MainFragmentNav : SimpleFragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    val binding by viewBinding(MainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.qrScannerButton.setOnClickListener { launchScanner() }
        binding.licensesButton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            if (it.contents == null) {
                Toast.makeText(requireContext(), getString(R.string.scanner_error_message), Toast.LENGTH_LONG).show()
            } else {
                generateQRCode(it.contents)
            }
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    private fun launchScanner() {
        IntentIntegrator(requireActivity()).run {
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
            initiateScan()
        }
    }

    private fun generateQRCode(content: String) {
        Toast.makeText(requireContext(), content, Toast.LENGTH_LONG).show()
        try {
            val bitmap = BarcodeEncoder().encodeBitmap(
                content,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels
            )
            binding.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }
}
