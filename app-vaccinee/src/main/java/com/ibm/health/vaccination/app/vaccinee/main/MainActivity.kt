package com.ibm.health.vaccination.app.vaccinee.main

import android.content.Intent
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.vaccination.app.BaseActivity
import com.ibm.health.common.vaccination.app.scanner.QRScannerActivity
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.onboarding.WelcomeFragmentNav

internal class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            if (vaccineeDeps.storage.onboardingDone) {
                navigator.push(MainFragmentNav())
            } else {
                navigator.push(WelcomeFragmentNav())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Forward ZXing's results
        if (IntentIntegrator.parseActivityResult(requestCode, resultCode, data) != null) {
            navigator.findFragment<MainFragment>()?.onActivityResult(requestCode, resultCode, data)
        }
    }
    fun launchScanner() {
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
