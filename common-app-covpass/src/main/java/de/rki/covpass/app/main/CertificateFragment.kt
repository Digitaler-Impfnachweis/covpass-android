/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ensody.reactivestate.android.autoRun
import com.ensody.reactivestate.dispatchers
import com.ensody.reactivestate.get
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.ibm.health.common.android.utils.buildState
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.app.storage.GroupedCertificatesList
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateBinding
import de.rki.covpass.app.storage.GroupedCertificates
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Vaccination
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize

@Parcelize
internal class CertificateFragmentNav(val certId: GroupedCertificatesId) : FragmentNav(CertificateFragment::class)

/**
 * Fragment which shows a [GroupedCertificates].
 */
internal class CertificateFragment : BaseFragment() {

    internal val args: CertificateFragmentNav by lazy { getArgs() }
    private val viewModel by buildState { CertificateViewModel(scope) }
    private val binding by viewBinding(CertificateBinding::inflate)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoRun {
            // TODO: Optimize this, so we only update if our cert has changed and not something else
            updateViews(get(covpassDeps.certRepository.certs))
        }
    }

    private fun updateViews(certificateList: GroupedCertificatesList) {
        val certId = args.certId
        val groupedCertificate = certificateList.getGroupedCertificates(certId) ?: return
        val mainCombinedCertificate = groupedCertificate.getMainCertificate()
        val mainCertificate = mainCombinedCertificate.covCertificate
        val fullProtection = mainCertificate.vaccination?.hasFullProtection == true

        launchWhenStarted {
            binding.certificateQrImageview.setImageBitmap(
                generateQRCode(mainCombinedCertificate.qrContent)
            )
        }

        val dgcEntry = mainCertificate.dgcEntry
        if (dgcEntry is Vaccination) {
            val complete = dgcEntry.isComplete
            binding.certificateStatusTextview.text = when {
                fullProtection -> getString(R.string.vaccination_start_screen_qrcode_complete_protection_subtitle)
                (complete && !fullProtection) -> getString(
                    R.string.vaccination_start_screen_qrcode_complete_from_date_subtitle,
                    mainCertificate.validDate.formatDateOrEmpty()
                )
                else -> getString(R.string.vaccination_start_screen_qrcode_incomplete_subtitle)
            }
        }

        val textColor = if (fullProtection) {
            R.color.onInfo
        } else {
            R.color.onBackground
        }
        context?.let {
            binding.certificateHeaderTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateNameTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateProtectionTextview.setTextColor(ContextCompat.getColor(it, textColor))
        }

        val backgroundColorResource = if (fullProtection) {
            R.color.info80
        } else {
            R.color.info20
        }
        val backgroundFaderResource = if (fullProtection) {
            R.drawable.common_gradient_card_fadeout_blue
        } else {
            R.drawable.common_gradient_card_fadeout_light_blue
        }
        context?.let {
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColorResource))
            binding.cardBottomFadeout.setBackgroundResource(backgroundFaderResource)
        }

        val activeRes = if (fullProtection) {
            R.drawable.star_white_fill
        } else {
            R.drawable.star_black_fill
        }
        val inactiveRes = if (fullProtection) {
            R.drawable.star_white
        } else {
            R.drawable.star_black
        }
        val favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) {
            activeRes
        } else {
            inactiveRes
        }

        binding.certificateFavoriteButton.setImageResource(favoriteIconResource)
        binding.certificateFavoriteButton.setOnClickListener {
            viewModel.onFavoriteClick(certId)
        }
        binding.certificateFavoriteButton.isVisible = certificateList.certificates.size > 1

        val headerText = if (fullProtection) {
            R.string.vaccination_full_immunization_title
        } else {
            R.string.vaccination_partial_immunization_title
        }
        binding.certificateHeaderTextview.text = getString(headerText)

        binding.certificateNameTextview.text = mainCertificate.fullName

        val protection = if (fullProtection) {
            R.string.vaccination_full_immunization_action_button
        } else {
            R.string.vaccination_partial_immunization_action_button
        }
        binding.certificateProtectionTextview.text = getString(protection)

        val arrowRightIconResource = if (fullProtection) {
            R.drawable.arrow_right_white
        } else {
            R.drawable.arrow_right_blue
        }
        binding.certificateArrowImageview.setImageResource(arrowRightIconResource)

        binding.certificateCardview.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }

        binding.certificateCardviewScrollContent.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }

        val statusIconResource = if (fullProtection) {
            R.drawable.main_cert_status_complete
        } else {
            R.drawable.main_cert_status_incomplete
        }
        binding.certificateStatusImageview.setImageResource(statusIconResource)
    }

    private suspend fun generateQRCode(qrContent: String): Bitmap {
        return dispatchers.default {
            BarcodeEncoder().encodeBitmap(
                qrContent,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels,
                mapOf(EncodeHintType.MARGIN to 0)
            )
        }
    }
}
