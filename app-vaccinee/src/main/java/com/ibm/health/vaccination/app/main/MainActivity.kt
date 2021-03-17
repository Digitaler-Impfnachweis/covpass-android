package com.ibm.health.vaccination.app.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.BaseViewModel
import com.ibm.health.common.android.utils.ViewModelOwner
import com.ibm.health.common.android.utils.buildBaseViewModel
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.vaccination.app.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.BarcodeEncoder

internal class MainActivityViewModel : BaseViewModel<MainActivityEvents>() {
    val state = MainActivityState(this)
}

internal class MainActivity : BaseActivity(), ViewModelOwner<MainActivityEvents>, MainActivityEvents {

    override val viewModel by buildBaseViewModel { MainActivityViewModel() }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.qrScannerBtn.setOnClickListener { viewModel.state.onLaunchQRScannerButtonClicked() }
    }

    override fun launchScanner() {
        IntentIntegrator(this).run {
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
            initiateScan()
        }
    }

    override fun generateQRCode(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show()
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

    override fun showError(res: Int) {
        Toast.makeText(this, getString(res), Toast.LENGTH_LONG).show()
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            viewModel.state.onQRCodeResultReceived(it)
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }
}
