/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.main

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
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
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.formatTimeOrEmpty
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
        val isMarkedAsFavorite = certificateList.isMarkedAsFavorite(certId)

        val certStatus = mainCombinedCertificate.status
        val isFavoriteButtonVisible = certificateList.certificates.size > 1

        launchWhenStarted {
            binding.certificateCard.qrCodeImage = generateQRCode(mainCombinedCertificate.qrContent)
        }

        when (val dgcEntry = mainCertificate.dgcEntry) {
            is Vaccination -> {
                when (dgcEntry.type) {
                    VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                        binding.certificateCard.vaccinationFullProtectionCard(
                            getString(R.string.certificates_overview_vaccination_certificate_title),
                            if (certStatus == CertValidationResult.ExpiryPeriod) {
                                getString(
                                    R.string.certificates_start_screen_qrcode_certificate_expires_subtitle,
                                    mainCertificate.validUntil.formatDateOrEmpty(),
                                    mainCertificate.validUntil.formatTimeOrEmpty()
                                )
                            } else {
                                getString(R.string.vaccination_start_screen_qrcode_complete_protection_subtitle)
                            },
                            getString(R.string.vaccination_full_immunization_action_button),
                            mainCertificate.fullName,
                            isMarkedAsFavorite,
                            certStatus,
                            !groupedCertificate.hasSeenBoosterDetailNotification &&
                                groupedCertificate.boosterResult == BoosterResult.Passed
                        )
                    }
                    VaccinationCertType.VACCINATION_COMPLETE -> {
                        binding.certificateCard.createVaccinationCompleteOrPartialCard(
                            getString(R.string.certificates_overview_vaccination_certificate_title),
                            if (certStatus == CertValidationResult.ExpiryPeriod) {
                                getString(
                                    R.string.certificates_start_screen_qrcode_certificate_expires_subtitle,
                                    mainCertificate.validUntil.formatDateOrEmpty(),
                                    mainCertificate.validUntil.formatTimeOrEmpty()
                                )
                            } else {
                                getString(
                                    R.string.vaccination_start_screen_qrcode_complete_from_date_subtitle,
                                    mainCertificate.validDate.formatDateOrEmpty()
                                )
                            },
                            getString(R.string.vaccination_partial_immunization_action_button),
                            mainCertificate.fullName,
                            isMarkedAsFavorite,
                            certStatus
                        )
                    }
                    VaccinationCertType.VACCINATION_INCOMPLETE -> {
                        binding.certificateCard.createVaccinationCompleteOrPartialCard(
                            getString(R.string.certificates_overview_vaccination_certificate_title),
                            if (certStatus == CertValidationResult.ExpiryPeriod) {
                                getString(
                                    R.string.certificates_start_screen_qrcode_certificate_expires_subtitle,
                                    mainCertificate.validUntil.formatDateOrEmpty(),
                                    mainCertificate.validUntil.formatTimeOrEmpty()
                                )
                            } else {
                                getString(R.string.vaccination_start_screen_qrcode_incomplete_subtitle)
                            },
                            getString(R.string.vaccination_partial_immunization_action_button),
                            mainCertificate.fullName,
                            isMarkedAsFavorite,
                            certStatus
                        )
                    }
                }
            }
            is TestCert -> {
                val test = mainCertificate.dgcEntry as TestCert
                binding.certificateCard.createTestCard(
                    if (test.testType == TestCert.PCR_TEST) {
                        getString(R.string.certificates_overview_pcr_test_certificate_message)
                    } else {
                        getString(R.string.certificates_overview_test_certificate_message)
                    },
                    if (certStatus == CertValidationResult.ExpiryPeriod) {
                        getString(
                            R.string.certificates_start_screen_qrcode_certificate_expires_subtitle,
                            mainCertificate.validUntil.formatDateOrEmpty(),
                            mainCertificate.validUntil.formatTimeOrEmpty()
                        )
                    } else {
                        test.sampleCollection?.toDeviceTimeZone()?.formatDateTime().orEmpty()
                    },
                    getString(R.string.test_certificate_action_button),
                    mainCertificate.fullName,
                    isMarkedAsFavorite,
                    certStatus
                )
            }
            is Recovery -> {
                val recovery = mainCertificate.dgcEntry as Recovery
                binding.certificateCard.createRecoveryCard(
                    getString(R.string.certificates_overview_recovery_certificate_title),
                    if (certStatus == CertValidationResult.ExpiryPeriod) {
                        getString(
                            R.string.certificates_start_screen_qrcode_certificate_expires_subtitle,
                            mainCertificate.validUntil.formatDateOrEmpty(),
                            mainCertificate.validUntil.formatTimeOrEmpty()
                        )
                    } else {
                        if (recovery.validFrom?.isAfter(LocalDate.now()) == true) {
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
                    },
                    getString(R.string.recovery_certificate_action_button),
                    mainCertificate.fullName,
                    isMarkedAsFavorite,
                    certStatus
                )
            }
            // .let{} to enforce exhaustiveness
        }.let {}

        binding.certificateCard.isFavoriteButtonVisible = isFavoriteButtonVisible
        binding.certificateCard.setOnFavoriteClickListener {
            viewModel.onFavoriteClick(args.certId)
        }
        binding.certificateCard.setOnCardClickListener {
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
