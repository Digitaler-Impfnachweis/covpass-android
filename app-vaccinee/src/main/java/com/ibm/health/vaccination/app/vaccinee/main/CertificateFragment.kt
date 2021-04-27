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
import com.ibm.health.vaccination.app.vaccinee.dependencies.vaccineeDeps
import com.ibm.health.vaccination.app.vaccinee.detail.DetailFragmentNav
import com.ibm.health.vaccination.app.vaccinee.storage.GroupedCertificatesList
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
            updateViews(get(vaccineeDeps.storage.certs))
        }
    }

    private fun updateViews(certificateList: GroupedCertificatesList) {
        val certId = getArgs<CertificateFragmentNav>().certId
        val groupedCertificate = certificateList.getGroupedCertificates(certId)
        val mainExtendedCertificate = groupedCertificate.getMainCertificate()
        val mainCertificate = groupedCertificate.getMainCertificate().vaccinationCertificate
        val complete = groupedCertificate.isComplete()

        launchWhenStarted {
            if (complete) {
                mainExtendedCertificate.validationQrContent?.let {
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
        }

        val backgroundColorResource = if (complete) R.color.info80 else R.color.info20
        context?.let {
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColorResource))
        }

        val activeRes = if (complete) R.drawable.star_white_fill else R.drawable.star_black_fill
        val inactiveRes = if (complete) R.drawable.star_white else R.drawable.star_black
        val favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) activeRes else inactiveRes

        binding.certificateFavoriteButton.setImageResource(favoriteIconResource)
        binding.certificateFavoriteButton.setOnClickListener {
            state.onFavoriteClick(certId)
        }
        binding.certificateFavoriteButton.isVisible = certificateList.certificates.size > 1

        binding.certificateNameTextview.text = mainCertificate.name

        val protection = R.string.certificate_protection
        binding.certificateProtectionTextview.text = getString(protection)

        val arrowRightIconResource = if (complete) R.drawable.arrow_right_white else R.drawable.arrow_right_blue
        binding.certificateArrowImageview.setImageResource(arrowRightIconResource)

        binding.certificateVaccinationStatusContainer.setOnClickListener {
            findNavigator().push(DetailFragmentNav(getArgs<CertificateFragmentNav>().certId))
        }

        val statusIconResource =
            if (complete) R.drawable.main_vaccination_status_complete else R.drawable.main_vaccination_status_incomplete
        binding.certificateVaccinationStatusImageview.setImageResource(statusIconResource)

        binding.certificateQrCardview.isInvisible = !complete
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
