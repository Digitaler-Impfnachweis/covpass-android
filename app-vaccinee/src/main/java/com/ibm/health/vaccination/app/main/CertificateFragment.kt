package com.ibm.health.vaccination.app.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.zxing.BarcodeFormat
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
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
// FIXME this is just a provisionally implementation
class CertificateFragmentNav(val demoIncompleteCertificate: Boolean) : FragmentNav(CertificateFragment::class)

internal class CertificateFragment : BaseFragment() {

    private val binding by viewBinding(CertificateBinding::inflate)
    private lateinit var qrContent: String
    private lateinit var vaccinationCertificate: VaccinationCertificate

    @ExperimentalSerializationApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXME this is just a provisionally implementation
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

        val incomplete = getArgs<CertificateFragmentNav>().demoIncompleteCertificate

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

        val qrVisibility = if (incomplete) View.INVISIBLE else View.VISIBLE
        binding.certificateQrCardview.visibility = qrVisibility

        if (!incomplete) {
            generateQRCode()
        }

        binding.certificateAddButton.isVisible = incomplete
        binding.certificateAddButton.setOnClickListener { (parentFragment as? MainFragment)?.launchScanner() }
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
