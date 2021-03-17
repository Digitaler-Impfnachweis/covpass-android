package com.ibm.health.vaccination.app.main

import com.google.zxing.integration.android.IntentResult
import com.ibm.health.common.android.utils.test.reactive.BaseStateTest
import com.ibm.health.vaccination.app.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class MainActivityStateTest :
    BaseStateTest<MainActivityEvents, MainActivityState<MainActivityEvents>>() {
    override val events: MainActivityEvents = mockk()
    override val state by lazy {
        MainActivityState(wrapperViewModel)
    }
    private val intentResult: IntentResult = mockk()

    @Before
    fun beforeTests() {
        mockkObject(intentResult)
        every { events.launchScanner() } returns Unit
        every { events.generateQRCode(any()) } returns Unit
        every { events.showError(any()) } returns Unit
    }

    @Test
    fun `view should be closed whenever the delete process is confirmed and executed`() =
        runBlockingTest {
            state.onLaunchQRScannerButtonClicked()
            verify { events.launchScanner() }
        }

    @Test
    fun `view should be closed whenever the delete process is confirmed and`() =
        runBlockingTest {
            every { intentResult.contents } returns QR_CODE_URL
            state.onQRCodeContentReceived(intentResult)
            verify { events.generateQRCode(QR_CODE_URL) }
        }

    @Test
    fun `view should be closed whenever the delete process is confirmed`() =
        runBlockingTest {
            every { intentResult.contents } returns null
            state.onQRCodeContentReceived(intentResult)
            verify { events.showError(SCANNER_ERROR_MESSAGE_RES) }
        }

    companion object {
        const val QR_CODE_URL = "https://qr/code/url"
        const val SCANNER_ERROR_MESSAGE_RES = R.string.scanner_error_message
    }
}
