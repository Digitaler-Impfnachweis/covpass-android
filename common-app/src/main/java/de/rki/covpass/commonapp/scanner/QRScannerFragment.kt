/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.savedInstanceState
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import com.journeyapps.barcodescanner.*
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.FragmentQrScannerBinding
import de.rki.covpass.commonapp.utils.getScreenSize
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted
import kotlin.math.min

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
public abstract class QRScannerFragment : BaseFragment(), DecoratedBarcodeView.TorchListener {

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

    protected val scanEnabled: MutableValueFlow<Boolean> by savedInstanceState(true)

    private val barcodeCallback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            withErrorReporting {
                if (scanEnabled.value) {
                    scanEnabled.value = false
                    onBarcodeResult(result)
                }
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private val isTorchOn: MutableValueFlow<Boolean> by savedInstanceState(false)

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
                findNavigator().popAll()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        decoratedBarcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(barcodeTypes)
        val screenSize = requireContext().getScreenSize()
        decoratedBarcodeView.barcodeView.framingRectSize = Size(screenSize.x, screenSize.y)
        binding.scannerCloseButton.setOnClickListener { requireActivity().onBackPressed() }

        decoratedBarcodeView.setTorchListener(this)
        binding.scannerFlashlightButton.isVisible = hasFlash()

        binding.scannerFlashlightButton.setOnClickListener {
            setTorch(!isTorchOn.value)
        }
        setTorch(isTorchOn.value)

        checkPermission()
        view.post {
            binding.scannerImageView.setImageDrawable(
                ScannerDrawable(
                    binding.zxingBarcodeScanner.width,
                    binding.zxingBarcodeScanner.height - binding.scannerCloseLayout.height,
                    resources.getDimension(R.dimen.grid_four)
                )
            )
        }
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

    private class ScannerDrawable(
        private val width: Int,
        private val height: Int,
        private val border: Float,
    ) : Drawable() {
        override fun draw(canvas: Canvas) {
            bounds.set(0, 0, width, height)
            val path = Path()
            path.fillType = Path.FillType.EVEN_ODD
            path.addRect(RectF(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat()), Path.Direction.CCW)

            val squareSize = min(bounds.width(), bounds.height()) - 2 * border

            path.addRect(
                RectF(
                    bounds.width() / 2f - squareSize / 2,
                    bounds.height() / 2f - squareSize / 2,
                    bounds.width() / 2f + squareSize / 2,
                    bounds.height() / 2f + squareSize / 2,
                ),
                Path.Direction.CW
            )

            val paint = Paint()
            paint.color = Color.BLACK
            paint.alpha = 57
            canvas.drawPath(path, paint)
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        override fun getOpacity(): Int {
            return PixelFormat.OPAQUE
        }
    }

    private fun hasFlash(): Boolean =
        activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) ?: false

    private fun setTorch(on: Boolean) {
        if (on) {
            decoratedBarcodeView.setTorchOn()
        } else {
            decoratedBarcodeView.setTorchOff()
        }
    }

    override fun onTorchOn() {
        isTorchOn.value = true
    }

    override fun onTorchOff() {
        isTorchOn.value = false
    }
}
