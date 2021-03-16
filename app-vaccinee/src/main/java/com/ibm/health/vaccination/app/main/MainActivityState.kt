package com.ibm.health.vaccination.app.main

import com.google.zxing.integration.android.IntentResult
import com.ibm.health.common.android.utils.BaseEvents
import com.ibm.health.common.android.utils.State

@Suppress("EXPERIMENTAL_API_USAGE")
open class MainActivityState<T : MainActivityEvents>(
    parentState: State<T>,
) : State<T> by parentState {

    fun onLaunchQRScannerButtonClicked() {
        eventNotifier {
            launchScanner()
        }
    }

    fun onQRCodeContentReceived(result: IntentResult) {
        eventNotifier {
            if (result.contents == null) {
                showError("Canceled")
            } else {
                generateQRCode(result.contents)
            }
        }
    }
}

interface MainActivityEvents : BaseEvents {
    fun launchScanner()
    fun generateQRCode(content: String)
    fun showError(message: String)
}
