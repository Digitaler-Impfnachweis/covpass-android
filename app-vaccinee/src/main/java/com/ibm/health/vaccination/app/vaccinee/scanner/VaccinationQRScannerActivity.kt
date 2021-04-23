package com.ibm.health.vaccination.app.vaccinee.scanner

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity

/**
 * Capture activity for ZXing, pushes VaccinationQRScannerFragment to the navigator.
 */
class VaccinationQRScannerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            navigator.push(VaccinationQRScannerFragmentNav())
        }
    }
}
