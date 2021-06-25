/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.scanner

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.savedInstanceState
import com.ensody.reactivestate.withErrorReporting
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import com.journeyapps.barcodescanner.*
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.databinding.FragmentQrScannerBinding
import de.rki.covpass.commonapp.utils.getScreenSize
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
public abstract class QRScannerFragment : BaseFragment() {

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

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
        checkPermission()
    }

    protected abstract fun onBarcodeResult(result: BarcodeResult)

    private fun checkPermission() {
        if (isCameraPermissionGranted(requireContext())) {
            startScanning()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
