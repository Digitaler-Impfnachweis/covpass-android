package com.ibm.health.common.vaccination.app.scanner

import android.os.Bundle
import android.view.View
import com.ibm.health.common.android.utils.getScreenSize
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.databinding.FragmentQrScannerBinding
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.Size
import kotlinx.parcelize.Parcelize

/**
 * QR Scanner Fragment extending from BaseFragment to display a custom layout form scanner view.
 */
@Parcelize
public class CustomScannerFragmentNav : FragmentNav(CustomScannerFragment::class)

public class CustomScannerFragment : BaseFragment() {
    private lateinit var capture: CaptureManager

    private val binding by viewBinding(FragmentQrScannerBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenSize = requireContext().getScreenSize()
        binding.zxingBarcodeScanner.barcodeView.framingRectSize = Size(screenSize.x, screenSize.y)
        binding.scannerBackButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.scannerCloseButton.setOnClickListener { requireActivity().onBackPressed() }
        capture = CaptureManager(requireActivity(), binding.zxingBarcodeScanner)
        capture.initializeFromIntent(requireActivity().intent, savedInstanceState)
        capture.setShowMissingCameraPermissionDialog(false)
        capture.decode()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
