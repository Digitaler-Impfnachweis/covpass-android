package com.ibm.health.vaccination.app.vaccinee.common

import android.content.Intent
import com.ensody.reactivestate.get
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.vaccination.app.BaseFragment

abstract class ScannerResultFragment : BaseFragment() {

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            if (it.contents != null) {
                handleQrContent(it.contents)
            }
            // Else the backbutton was pressed, nothing to do
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    protected abstract fun handleQrContent(qrContent: String)
}
