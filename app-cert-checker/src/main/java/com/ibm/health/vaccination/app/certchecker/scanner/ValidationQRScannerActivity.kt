package com.ibm.health.vaccination.app.certchecker.scanner

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity

/**
 * Capture activity for ZXing, pushes ValidationQRScannerFragment to the navigator.
 */
class ValidationQRScannerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            navigator.push(ValidationQRScannerFragmentNav())
        }
    }
}
