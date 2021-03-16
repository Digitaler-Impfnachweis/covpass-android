package com.ibm.health.vaccination.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.vaccination.app.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.BarcodeEncoder

class MainActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.qrScannerBtn.setOnClickListener { launchScanner() }
    }

    private fun launchScanner() {
        IntentIntegrator(this).run {
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
            initiateScan()
        }
    }

    private fun generateQRCode(content: String) {
        try {
            val bitmap = BarcodeEncoder().encodeBitmap(
                content,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels
            )
            binding.qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
                generateQRCode(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
