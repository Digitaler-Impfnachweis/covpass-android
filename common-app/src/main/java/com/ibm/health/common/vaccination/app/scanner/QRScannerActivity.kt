package com.ibm.health.common.vaccination.app.scanner

import android.os.Bundle
import com.ibm.health.common.vaccination.app.BaseActivity

/**
 * QR Scanner Activity pushes QRScannerFragment to the navigator.
 */
public class QRScannerActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (navigator.isEmpty() && savedInstanceState == null) {
            navigator.push(CustomScannerFragmentNav())
        }
    }
}
