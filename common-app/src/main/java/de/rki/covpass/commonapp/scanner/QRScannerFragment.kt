/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.scanner

import android.Manifest
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.window.WindowManager
import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.android.savedInstanceState
import com.ensody.reactivestate.get
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.findNavigator
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.FragmentQrScannerBinding
import de.rki.covpass.commonapp.utils.isCameraPermissionGranted
import kotlinx.coroutines.flow.collect
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
public abstract class QRScannerFragment : BaseFragment() {

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

    protected val scanEnabled: MutableValueFlow<Boolean> by savedInstanceState(true)

    private val scannerViewModel: QRScannerViewModel by reactiveState { QRScannerViewModel(scope) }

    override val announcementAccessibilityRes: Int = R.string.accessibility_scan_camera_announce

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var windowManager: WindowManager
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null

    private val isTorchOn: MutableValueFlow<Boolean> by savedInstanceState(false)

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
        binding.scannerCloseButton.setOnClickListener { requireActivity().onBackPressed() }

        autoRun {
            updateTorchView(get(isTorchOn))
        }

        launchWhenStarted {
            scannerViewModel.scanResults.collect { result ->
                if (scanEnabled.value) {
                    scanEnabled.value = false
                    onBarcodeResult(result)
                }
            }
        }

        binding.scannerFlashlightButton.setOnClickListener {
            setTorch(!isTorchOn.value)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        windowManager = WindowManager(view.context)

        checkPermission()
        view.post {
            binding.scannerImageView.setImageDrawable(
                ScannerDrawable(
                    binding.barcodeScanner.width,
                    binding.barcodeScanner.height - binding.scannerCloseLayout.height,
                    resources.getDimension(R.dimen.grid_four)
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        camera?.let {
            setTorch(isTorchOn.value)
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Back and front camera are unavailable")
                }
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext()),
        )
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun bindCameraUseCases() {
        val metrics = windowManager.getCurrentWindowMetrics().bounds
        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        val rotation = binding.barcodeScanner.display.rotation
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) {
                    if (scanEnabled.value) {
                        scannerViewModel.onNewImage(it)
                    } else {
                        it.close()
                    }
                }
            }

        cameraProvider.unbindAll()

        withErrorReporting {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            initFocusControl()
            binding.scannerFlashlightButton.isVisible = hasFlash()
            setTorch(isTorchOn.value)
            preview.setSurfaceProvider(binding.barcodeScanner.surfaceProvider)
        }
    }

    protected abstract fun onBarcodeResult(qrCode: String)

    private fun checkPermission() {
        if (isCameraPermissionGranted(requireContext())) {
            startScanning()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanning() {
        binding.barcodeScanner.post {
            displayId = binding.barcodeScanner.display.displayId
            setUpCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
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

    private fun hasFlash(): Boolean = camera?.cameraInfo?.hasFlashUnit() ?: false

    private fun setTorch(on: Boolean) {
        isTorchOn.value = on
        camera?.cameraControl?.enableTorch(on)
    }

    private fun updateTorchView(on: Boolean) {
        if (on) {
            binding.scannerFlashlightButton.contentDescription =
                resources.getString(R.string.accessibility_scan_camera_torch_off)
        } else {
            binding.scannerFlashlightButton.contentDescription =
                resources.getString(R.string.accessibility_scan_camera_torch_on)
        }
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun initFocusControl(): Boolean {
        val metrics = windowManager.getCurrentWindowMetrics().bounds

        // do initial autofocus
        val initialAutoFocusPoint =
            SurfaceOrientedMeteringPointFactory(metrics.width().toFloat(), metrics.height().toFloat())
                .createPoint(metrics.exactCenterX(), metrics.exactCenterY())

        val initialAutoFocusAction = FocusMeteringAction.Builder(
            initialAutoFocusPoint, FocusMeteringAction.FLAG_AF
        ).build()
        camera?.cameraControl?.startFocusAndMetering(initialAutoFocusAction)

        // do on-tap autofocus
        binding.barcodeScanner.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val autofocusPoint =
                        SurfaceOrientedMeteringPointFactory(view.width.toFloat(), view.height.toFloat())
                            .createPoint(motionEvent.x, motionEvent.y)
                    camera?.cameraControl?.startFocusAndMetering(
                        FocusMeteringAction.Builder(
                            autofocusPoint, FocusMeteringAction.FLAG_AF
                        ).apply {
                            // only focus when user taps
                            disableAutoCancel()
                        }.build()
                    )
                    view.performClick()
                }
                else -> false
            }
        }

        return true
    }

    private companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
