package com.ibm.health.common.vaccination.app.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.savedInstanceState
import com.ensody.reactivestate.withErrorReporting
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.FragmentQrScannerBinding
import com.ibm.health.common.vaccination.app.utils.getScreenSize
import com.journeyapps.barcodescanner.*

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
public abstract class QRScannerFragment : BaseFragment() {

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

    protected abstract val loadingText: Int

    protected val scanEnabled: MutableValueFlow<Boolean> by savedInstanceState(true)

    private val barcodeCallback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            withErrorReporting(::onError) {
                if (scanEnabled.value) {
                    scanEnabled.value = false
                    onBarcodeResult(result)
                }
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private val decoratedBarcodeView: DecoratedBarcodeView get() = binding.zxingBarcodeScanner

    private var barcodeTypes: List<BarcodeFormat> = listOf(
        BarcodeFormat.QR_CODE,
        BarcodeFormat.AZTEC,
        BarcodeFormat.DATA_MATRIX,
    )

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startScanning()
            } else {
                findNavigator().pop()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decoratedBarcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(barcodeTypes)
        val screenSize = requireContext().getScreenSize()
        decoratedBarcodeView.barcodeView.framingRectSize = Size(screenSize.x, screenSize.y)
        binding.scannerCloseButton.setOnClickListener { requireActivity().onBackPressed() }
        checkPermission(Manifest.permission.CAMERA) { startScanning() }
        binding.loadingText.setText(loadingText)
    }

    protected abstract fun onBarcodeResult(result: BarcodeResult)

    override fun setLoading(isLoading: Boolean) {
        binding.loadingScreen.isVisible = isLoading
    }

    private fun checkPermission(targetPermission: String, targetAction: () -> Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(requireContext(), targetPermission) -> targetAction.invoke()
            else -> requestPermissionLauncher.launch(targetPermission)
        }
    }

    private fun startScanning() {
        decoratedBarcodeView.barcodeView.decodeContinuous(barcodeCallback)
    }

    override fun onResume() {
        super.onResume()
        decoratedBarcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        decoratedBarcodeView.pause()
    }
}
