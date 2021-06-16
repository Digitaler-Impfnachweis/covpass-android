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
import com.ensody.reactivestate.android.reactiveState
import com.ensody.reactivestate.dispatchers
import com.ensody.reactivestate.get
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.ibm.health.common.android.utils.viewBinding
import com.ibm.health.common.navigation.android.FragmentNav
import com.ibm.health.common.navigation.android.findNavigator
import com.ibm.health.common.navigation.android.getArgs
import com.journeyapps.barcodescanner.BarcodeEncoder
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
internal class CertificateFragmentNav(val certId: GroupedCertificatesId) : FragmentNav(CertificateFragment::class)

/**
 * Fragment which shows a [CovCertificate]
 */
internal class CertificateFragment : BaseFragment() {

    internal val args: CertificateFragmentNav by lazy { getArgs() }
    private val viewModel by reactiveState { CertificateViewModel(scope) }
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
        val headerText: Int
        val textColor: Int
        val backgroundColor: Int
        val cardFadeout: Int
        val certificateStatus: String
        val certificateStatusIcon: Int
        val certificateProtectionText: Int
        var arrowIcon: Int = R.drawable.arrow_right_white
        var favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) {
            R.drawable.star_white_fill
        } else {
            R.drawable.star_white
        }

        launchWhenStarted {
            binding.certificateQrImageview.setImageBitmap(
                generateQRCode(mainCombinedCertificate.qrContent)
            )
        }

        val dgcEntry = mainCertificate.dgcEntry
        when (dgcEntry) {
            is Vaccination -> {
                when (dgcEntry.type) {
                    VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                        headerText = R.string.certificates_overview_vaccination_certificate_title
                        certificateStatus =
                            getString(R.string.vaccination_start_screen_qrcode_complete_protection_subtitle)
                        cardFadeout = R.drawable.common_gradient_card_fadeout_blue
                        certificateProtectionText = R.string.vaccination_full_immunization_action_button
                        certificateStatusIcon = R.drawable.main_cert_status_complete
                        textColor = R.color.onInfo
                        backgroundColor = R.color.info70
                    }
                    VaccinationCertType.VACCINATION_COMPLETE -> {
                        headerText = R.string.certificates_overview_vaccination_certificate_title
                        certificateStatus =
                            getString(
                                R.string.vaccination_start_screen_qrcode_complete_from_date_subtitle,
                                mainCertificate.validDate.formatDateOrEmpty()
                            )
                        favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) {
                            R.drawable.star_black_fill
                        } else {
                            R.drawable.star_black
                        }
                        cardFadeout = R.drawable.common_gradient_card_fadeout_light_blue
                        certificateProtectionText = R.string.vaccination_partial_immunization_action_button
                        arrowIcon = R.drawable.arrow_right_blue
                        certificateStatusIcon = R.drawable.main_cert_status_incomplete
                        textColor = R.color.onBackground
                        backgroundColor = R.color.info20
                    }
                    VaccinationCertType.VACCINATION_INCOMPLETE -> {
                        headerText = R.string.certificates_overview_vaccination_certificate_title
                        certificateStatus = getString(R.string.vaccination_start_screen_qrcode_incomplete_subtitle)
                        favoriteIconResource = if (certificateList.isMarkedAsFavorite(certId)) {
                            R.drawable.star_black_fill
                        } else {
                            R.drawable.star_black
                        }
                        cardFadeout = R.drawable.common_gradient_card_fadeout_light_blue
                        certificateProtectionText = R.string.vaccination_partial_immunization_action_button
                        arrowIcon = R.drawable.arrow_right_blue
                        certificateStatusIcon = R.drawable.main_cert_status_incomplete
                        textColor = R.color.onBackground
                        backgroundColor = R.color.info20
                    }
                }
            }
            is Test -> {
                val test = mainCertificate.dgcEntry as Test
                certificateStatus = test.sampleCollection?.toDeviceTimeZone()?.formatDateTime().orEmpty()
                headerText = when (test.testType) {
                    Test.PCR_TEST -> R.string.certificates_overview_pcr_test_certificate_message
                    else -> R.string.certificates_overview_test_certificate_message
                }
                cardFadeout = R.drawable.common_gradient_card_fadeout_purple
                certificateProtectionText = R.string.test_certificate_action_button
                certificateStatusIcon = R.drawable.main_cert_test
                textColor = R.color.onInfo
                backgroundColor = R.color.test_certificate_background
            }
            is Recovery -> {
                val recovery = mainCertificate.dgcEntry as Recovery
                certificateStatus = if (recovery.validFrom?.isAfter(LocalDate.now()) == true) {
                    getString(
                        R.string.certificates_overview_recovery_certificate_valid_from_date,
                        recovery.validFrom.formatDateOrEmpty()
                    )
                } else {
                    getString(
                        R.string.certificates_overview_recovery_certificate_valid_until_date,
                        recovery.validUntil.formatDateOrEmpty()
                    )
                }
                headerText = R.string.certificates_overview_recovery_certificate_title
                cardFadeout = R.drawable.common_gradient_card_fadeout_dark_blue
                certificateProtectionText = R.string.recovery_certificate_action_button
                certificateStatusIcon = R.drawable.main_cert_recovery
                textColor = R.color.onInfo
                backgroundColor = R.color.brandAccent90
            }
            // .let{} to enforce exhaustiveness
        }.let {}
        binding.certificateHeaderTextview.text = getString(headerText)
        binding.certificateStatusTextview.text = certificateStatus
        binding.certificateStatusImageview.setImageResource(certificateStatusIcon)
        binding.cardBottomFadeout.setBackgroundResource(cardFadeout)
        binding.certificateArrowImageview.setImageResource(arrowIcon)
        binding.certificateProtectionTextview.text = getString(certificateProtectionText)
        binding.certificateFavoriteButton.setImageResource(favoriteIconResource)
        context?.let {
            binding.certificateNameTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateProtectionTextview.setTextColor(ContextCompat.getColor(it, textColor))
            binding.certificateCardview.setCardBackgroundColor(ContextCompat.getColor(it, backgroundColor))
        }

        binding.certificateFavoriteButton.setOnClickListener {
            viewModel.onFavoriteClick(certId)
        }
        binding.certificateFavoriteButton.isVisible = certificateList.certificates.size > 1
        binding.certificateNameTextview.text = mainCertificate.fullName

        binding.certificateCardview.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }

        binding.certificateCardviewScrollContent.setOnClickListener {
            findNavigator().push(DetailFragmentNav(args.certId))
        }
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
