package com.ibm.health.vaccination.app.certchecker.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.OpenSourceLicenseFragmentNav
import com.ibm.health.common.vaccination.app.scanner.QRScannerActivity
import com.ibm.health.vaccination.app.certchecker.R
import kotlinx.parcelize.Parcelize
import com.ibm.health.vaccination.sdk.android.dependencies.sdkDeps
import com.ibm.health.vaccination.app.certchecker.databinding.CheckerMainBinding
import com.ibm.health.vaccination.sdk.android.qr.models.Vaccination
import com.ibm.health.vaccination.sdk.android.qr.models.ValidationCertificate
import java.util.*

@Parcelize
class MainFragmentNav : FragmentNav(MainFragment::class)

internal class MainFragment : BaseFragment() {

    private val binding by viewBinding(CheckerMainBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mainSettingsImagebutton.setOnClickListener { findNavigator().push(OpenSourceLicenseFragmentNav()) }
        binding.mainCheckCertButton.setOnClickListener { launchScanner() }

        // FIXME use correct date
        val date = Calendar.getInstance().getTime().toString()
        val updateString = String.format(resources.getString(R.string.main_availability_last_update), date)
        binding.mainAvailabilityLastUpdateTextview.text = updateString
    }

    // Get the scanner results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.let {
            if (it.contents != null) {
                // FIXME this is just a provisionally implementation
                showDialog(sdkDeps.qrCoder.decodeValidationCert(it.contents))
            }
            // Else the backbutton was pressed, nothing to do
        } ?: super.onActivityResult(requestCode, resultCode, data)
    }

    // FIXME this will be moved to sdk later on
    private fun launchScanner() {
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

    private fun showDialog(validationCertificate: ValidationCertificate) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .apply {
                validationCertificate.let {
                    setMessage(
                        "name = ${it.name}\n" +
                            "birthDate = ${it.birthDate}\n" +
                            generateVaccinationStrings(it.vaccination) +
                            "issuer = ${it.issuer}\n" +
                            "id = ${it.id}\n" +
                            "validUntil = ${it.validUntil}\n"
                    )
                }
                show()
            }
    }

    private fun generateVaccinationStrings(vaccinations: List<Vaccination>): String =
        vaccinations.joinToString("") {
            "targetDisease = ${it.targetDisease}\n" +
                "vaccineCode = ${it.vaccineCode}\n" +
                "product = ${it.product}\n" +
                "manufacturer = ${it.manufacturer}\n" +
                "series = ${it.series}\n" +
                "occurence = ${it.occurence}\n" +
                "country ${it.country}\n"
        }
}
