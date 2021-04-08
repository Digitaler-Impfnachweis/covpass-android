package com.ibm.health.vaccination.app.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.common.vaccination.app.handleError
import com.ibm.health.vaccination.app.R
import com.ibm.health.vaccination.app.databinding.CertificateBinding
import com.ibm.health.vaccination.app.detail.DetailFragmentNav
import com.ibm.health.vaccination.app.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.decode.QRDecoder
import com.ibm.health.vaccination.sdk.android.qr.decode.models.VaccinationCertificate
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import java.lang.IllegalStateException

@Parcelize
class CertificateFragmentNav : FragmentNav(CertificateFragment::class)

internal class CertificateFragment : BaseFragment() {

    private val binding by viewBinding(CertificateBinding::inflate)
    private lateinit var qrContent: String
    private lateinit var vaccinationCertificate: VaccinationCertificate

    @ExperimentalSerializationApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Storage.getQrContent()?.let {
            qrContent = it
        } ?: run {
            throw IllegalStateException()
        }
        try {
            vaccinationCertificate = QRDecoder().decode(qrContent)
        } catch (throwable: Throwable) {
            handleError(throwable, childFragmentManager)
        }
        Storage.setVaccinationCertificate(vaccinationCertificate)

        // FIXME this is just a provisionally implementation
        binding.certificateHeaderTextview.text = "Covid-19"
        binding.certificateFavoriteButton.setImageResource(R.drawable.star_white_fill)
        binding.certificateFavoriteButton.setOnClickListener {
            binding.certificateFavoriteButton.setImageResource(R.drawable.star_white)
            Toast.makeText(requireContext(), "Work in progress...", Toast.LENGTH_SHORT).show()
        }
        binding.certificateNameTextview.text = "Gregor Mustermann"
        binding.certificateProtectionTextview.text = "Impfschutz vollständig"
        binding.certificateSeriesTextview.text = "2/2 Impfungen"
        binding.certificateVaccinationStatusContainer.setOnClickListener { findNavigator().push(DetailFragmentNav()) }
        generateQRCode()
    }

    private fun generateQRCode() {
        // FIXME replace this with the simplified validation certificate
        try {
            val bitmap = BarcodeEncoder().encodeBitmap(
                qrContent,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels
            )
            binding.certificateQrImageview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // FIXME handle error
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }
}
