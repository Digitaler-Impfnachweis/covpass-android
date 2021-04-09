package com.ibm.health.vaccination.app.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.ensody.reactivestate.dispatchers
import com.google.zxing.BarcodeFormat
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.vaccination.app.R
import com.ibm.health.vaccination.app.databinding.CertificateBinding
import com.ibm.health.vaccination.app.detail.DetailFragmentNav
import com.ibm.health.vaccination.app.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.QRDecoder
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi

@Parcelize
// FIXME this is just a provisionally implementation
class CertificateFragmentNav(val demoIncompleteCertificate: Boolean) : FragmentNav(CertificateFragment::class)

internal class CertificateFragment : BaseFragment() {

    private val binding by viewBinding(CertificateBinding::inflate)

    @ExperimentalSerializationApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val incomplete = getArgs<CertificateFragmentNav>().demoIncompleteCertificate

        // FIXME this is just a provisional implementation
        launchWhenStarted {
            val qrContent = Storage.getQrContent() ?: throw IllegalStateException()
            if (!incomplete) {
                generateQRCode(qrContent)
            }
            val vaccinationCertificate = QRDecoder().decode(qrContent)
            Storage.setVaccinationCertificate(vaccinationCertificate)
        }

        val textColor = if (incomplete) R.color.onBackground else R.color.onInfo
        context?.let {
            binding.certificateHeaderTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateNameTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateProtectionTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateSeriesTextview.setTextColor(ContextCompat.getColor(it, textColor))
        }

        val backgroundColorResource = if (incomplete) R.color.info20 else R.color.info80
        context?.let {
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColorResource))
        }

        val favoriteIconActiveResource = if (incomplete) R.drawable.star_blue_fill else R.drawable.star_white_fill
        binding.certificateFavoriteButton.setImageResource(favoriteIconActiveResource)

        val favoriteIconInactiveResource = if (incomplete) R.drawable.star_blue else R.drawable.star_white
        binding.certificateFavoriteButton.setOnClickListener {
            binding.certificateFavoriteButton.setImageResource(favoriteIconInactiveResource)
            Toast.makeText(requireContext(), "Work in progress...", Toast.LENGTH_SHORT).show()
        }

        val name = if (incomplete) "Mara Mustermann" else "Max Mustermann"
        binding.certificateNameTextview.text = name

        val protection = if (incomplete) "Impfschutz nicht vollständig" else "Impfschutz vollständig"
        binding.certificateProtectionTextview.text = protection

        val series = if (incomplete) "1/2 Impfungen" else "2/2 Impfungen"
        binding.certificateSeriesTextview.text = series

        val arrowRightIconResource = if (incomplete) R.drawable.arrow_right_blue else R.drawable.arrow_right_white
        binding.certificateArrowImageview.setImageResource(arrowRightIconResource)

        binding.certificateVaccinationStatusContainer.setOnClickListener { findNavigator().push(DetailFragmentNav()) }

        val statusIconResource =
            if (incomplete) R.drawable.vaccination_status_incomplete else R.drawable.vaccination_status_complete
        binding.certificateVaccinationStatusImageview.setImageResource(statusIconResource)

        binding.certificateQrCardview.isInvisible = incomplete

        binding.certificateAddButton.isVisible = incomplete
        binding.certificateAddButton.setOnClickListener { (parentFragment as? MainFragment)?.launchScanner() }
    }

    private suspend fun generateQRCode(qrContent: String) {
        // FIXME replace this with the simplified validation certificate
        try {
            val bitmap = dispatchers.default {
                BarcodeEncoder().encodeBitmap(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.widthPixels
                )
            }
            binding.certificateQrImageview.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // FIXME handle error
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }
}
