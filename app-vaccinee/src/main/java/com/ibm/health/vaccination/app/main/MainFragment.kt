package com.ibm.health.vaccination.app.main

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayoutMediator
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.common.vaccination.app.extensions.stripUnderlines
import com.ibm.health.common.vaccination.app.scanner.QRScannerActivity
import com.ibm.health.vaccination.app.R
import com.ibm.health.vaccination.app.databinding.VaccineeMainBinding
import com.ibm.health.vaccination.app.storage.Storage
import kotlinx.parcelize.Parcelize

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(VaccineeMainBinding::inflate)
    private lateinit var fragmentStateAdapter: CertificateFragmentStateAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainAddButton.setOnClickListener { launchScanner() }
        binding.mainEmptyButton.setOnClickListener { launchScanner() }
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
        binding.mainFaqShowAllTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqShowAllTextview.stripUnderlines()
        binding.mainFaqUseCertTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqUseCertTextview.stripUnderlines()
        binding.mainFaqGetQrCodeTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqGetQrCodeTextview.stripUnderlines()
        binding.mainFaqDataUsageTextview.movementMethod = LinkMovementMethod.getInstance()
        binding.mainFaqDataUsageTextview.stripUnderlines()
        fragmentStateAdapter = CertificateFragmentStateAdapter(this)
        binding.mainViewPager.adapter = fragmentStateAdapter
        TabLayoutMediator(binding.mainTabLayout, binding.mainViewPager) { tab, position ->
            // no special tab config necessary
        }.attach()
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            if (it.contents == null) {
                Toast.makeText(requireContext(), getString(R.string.scanner_error_message), Toast.LENGTH_LONG).show()
            } else {
                Storage.setQrContent(it.contents)
                binding.mainEmptyCardview.isVisible = false
                binding.mainViewPagerContainer.isVisible = true
                // FIXME this is just a provisionally implementation
                fragmentStateAdapter.addFragment(CertificateFragmentNav(true).build())
                fragmentStateAdapter.addFragment(CertificateFragmentNav(false).build())
            }
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    fun launchScanner() {
        IntentIntegrator(requireActivity()).run {
            captureActivity = QRScannerActivity::class.java
            setDesiredBarcodeFormats(
                listOf(BarcodeFormat.QR_CODE.name, BarcodeFormat.DATA_MATRIX.name, BarcodeFormat.AZTEC.name)
            )
            setOrientationLocked(false)
            setPrompt("")
            setBeepEnabled(false)
            initiateScan()
        }
    }
}
