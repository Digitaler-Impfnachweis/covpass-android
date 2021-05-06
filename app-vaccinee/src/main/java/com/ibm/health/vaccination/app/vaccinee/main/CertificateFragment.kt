package com.ibm.health.vaccination.app.vaccinee.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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

class CertificateFragment : BaseFragment() {

    internal val args: CertificateFragmentNav by lazy { getArgs() }
    private val state by buildState { CertificateState(scope) }
    private val binding by viewBinding(CertificateBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoRun {
            // TODO: Optimize this, so we only update if our cert has changed and not something else
            updateViews(get(vaccineeDeps.certRepository.certs), get(vaccineeDeps.certRefreshService.failingCertIds))
        }
    }

    private fun updateViews(certificateList: GroupedCertificatesList, failingCertIds: Set<String>) {
        val certId = args.certId
        val groupedCertificate = certificateList.getGroupedCertificates(certId) ?: return
        val mainExtendedCertificate = groupedCertificate.getMainCertificate()
        val mainCertificate = groupedCertificate.getMainCertificate().vaccinationCertificate
        val complete = groupedCertificate.isComplete()

        launchWhenStarted {
            if (complete) {
                mainExtendedCertificate.validationQrContent?.let {
                    binding.certificateQrImageview.setImageBitmap(generateQRCode(it))
                }
            }
        }

        val textColor = if (complete) R.color.onInfo else R.color.onBackground
        context?.let {
            binding.certificateHeaderTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateNameTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateProtectionTextview.setTextColor(ContextCompat.getColor(it, textColor))
        }

        val backgroundColorResource = if (complete) R.color.info80 else R.color.info20
        val backgroundFaderResource = if (complete)
            R.drawable.common_gradient_card_fadeout_blue else R.drawable.common_gradient_card_fadeout_light_blue
        context?.let {
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColorResource))
            binding.cardBottomFadeout.setBackgroundResource(backgroundFaderResource)
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

        binding.certificateCardview.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }

        binding.certificateCardviewScrollContent.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }

        val statusIconResource =
            if (complete) R.drawable.main_vaccination_status_complete else R.drawable.main_vaccination_status_incomplete
        binding.certificateVaccinationStatusImageview.setImageResource(statusIconResource)

        val qrCodeIsMissing = groupedCertificate.getMainCertificate().validationQrContent == null
        binding.certificateQrCardview.isVisible = complete && !qrCodeIsMissing
        binding.certificateLoadingContainer.isVisible = complete && qrCodeIsMissing
        val loadingTextRes = if (failingCertIds.contains(groupedCertificate.getMainCertId())) {
            R.string.certificate_error_message_general
        } else {
            R.string.certificate_error_message_connection
        }
        binding.certificateLoadingText.setText(loadingTextRes)
    }

    // FIXME move to SDK
    private suspend fun generateQRCode(qrContent: String): Bitmap {
        return dispatchers.default {
            BarcodeEncoder().encodeBitmap(
                qrContent,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels
            )
        }
    }
}
