package com.ibm.health.vaccination.app.main

import androidx.annotation.StringRes
import com.google.zxing.integration.android.IntentResult
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.State
import com.ibm.health.vaccination.app.R

@Suppress("EXPERIMENTAL_API_USAGE")
internal class MainActivityState<T : MainActivityEvents>(
    parentState: State<T>,
) : State<T> by parentState {

    fun onLaunchQRScannerButtonClicked() {
        eventNotifier {
            launchScanner()
        }
    }

    fun onQRCodeResultReceived(result: IntentResult) {
        eventNotifier {
            if (result.contents == null) {
                showError(R.string.scanner_error_message)
            } else {
                generateQRCode(result.contents)
            }
        }
    }
}

interface MainActivityEvents : BaseEvents {
    fun launchScanner()
    fun generateQRCode(content: String)
    fun showError(@StringRes res: Int)
}
