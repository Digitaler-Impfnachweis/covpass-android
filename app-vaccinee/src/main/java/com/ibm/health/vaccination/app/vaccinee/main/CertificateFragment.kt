package com.ibm.health.vaccination.app.vaccinee.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.dispatchers
import com.ensody.reactivestate.get
import com.google.zxing.BarcodeFormat
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.ibm.health.common.vaccination.app.BaseFragment
import com.ibm.health.vaccination.app.vaccinee.R
import com.ibm.health.vaccination.app.vaccinee.databinding.CertificateBinding
import com.ibm.health.vaccination.app.vaccinee.detail.DetailFragmentNav
import com.ibm.health.vaccination.app.vaccinee.storage.Storage
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificateList
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize

@Parcelize
class CertificateFragmentNav(val certId: String) : FragmentNav(CertificateFragment::class)

internal class CertificateFragment : BaseFragment() {

    private val state by buildState { CertificateState(scope) }
    private val binding by viewBinding(CertificateBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoRun {
            updateViews(get(Storage.certCache))
        }
    }

    private fun updateViews(certificateList: VaccinationCertificateList) {
        val certId = getArgs<CertificateFragmentNav>().certId
        val extendedCertificate = certificateList.getExtendedVaccinationCertificate(certId)
        val vaccinationCertificate = extendedCertificate.vaccinationCertificate
        val complete = vaccinationCertificate.isComplete()

        launchWhenStarted {
            if (complete) {
                extendedCertificate.validationQrContent?.let {
                    generateQRCode(it)
                }
                // FIXME handle case when the qrContent is not yet set, because the backend request failed e.g.
            }
        }

        val textColor = if (complete) R.color.onInfo else R.color.onBackground
        context?.let {
            binding.certificateHeaderTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateNameTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateProtectionTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateSeriesTextview.setTextColor(ContextCompat.getColor(it, textColor))
        }

        val backgroundColorResource = if (complete) R.color.info80 else R.color.info20
        context?.let {
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColorResource))
        }

        val activeRes = if (complete) R.drawable.star_white_fill else R.drawable.star_blue_fill
        val inactiveRes = if (complete) R.drawable.star_white else R.drawable.star_blue
        val favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) activeRes else inactiveRes

        binding.certificateFavoriteButton.setImageResource(favoriteIconResource)
        binding.certificateFavoriteButton.setOnClickListener {
            state.onFavoriteClick(certId)
        }

        binding.certificateNameTextview.text = vaccinationCertificate.name

        val protection =
            if (complete) R.string.certificate_protection_complete else R.string.certificate_protection_incomplete
        binding.certificateProtectionTextview.text = getString(protection)

        val completeVaccination =
            vaccinationCertificate.vaccination.firstOrNull { it.isComplete() }
        val mainVaccination = completeVaccination ?: vaccinationCertificate.vaccination.first()
        binding.certificateSeriesTextview.text = getString(R.string.certificate_series, mainVaccination.series)

        val arrowRightIconResource = if (complete) R.drawable.arrow_right_white else R.drawable.arrow_right_blue
        binding.certificateArrowImageview.setImageResource(arrowRightIconResource)

        binding.certificateVaccinationStatusContainer.setOnClickListener {
            findNavigator().push(DetailFragmentNav(getArgs<CertificateFragmentNav>().certId))
        }

        val statusIconResource =
            if (complete) R.drawable.vaccination_status_complete else R.drawable.vaccination_status_incomplete
        binding.certificateVaccinationStatusImageview.setImageResource(statusIconResource)

        binding.certificateQrCardview.isInvisible = !complete

        binding.certificateAddButton.isVisible = !complete
        binding.certificateAddButton.setOnClickListener { (requireActivity() as? MainActivity)?.launchScanner() }
    }

    // FIXME move this to state
    // FIXME move this to SDK and change return to Bitmap
    private suspend fun generateQRCode(qrContent: String) {
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
