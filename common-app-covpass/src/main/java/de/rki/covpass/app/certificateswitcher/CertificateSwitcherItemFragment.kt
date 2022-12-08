/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.certificateswitcher

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.ensody.reactivestate.android.autoRun
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
import de.rki.covpass.app.databinding.CertificateSwitcherItemBinding
import de.rki.covpass.app.dependencies.covpassDeps
import de.rki.covpass.app.detail.DetailFragmentNav
import de.rki.covpass.commonapp.BaseFragment
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.cert.models.MaskStatus
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.cert.models.VaccinationCertType
import de.rki.covpass.sdk.utils.hoursTillNow
import de.rki.covpass.sdk.utils.monthTillNow
import kotlinx.coroutines.invoke
import kotlinx.parcelize.Parcelize
import java.time.ZoneId

@Parcelize
internal class CertificateSwitcherItemFragmentNav(
    val certId: GroupedCertificatesId,
    val id: String,
) : FragmentNav(CertificateSwitcherItemFragment::class)

internal class CertificateSwitcherItemFragment : BaseFragment() {

    internal val args: CertificateSwitcherItemFragmentNav by lazy { getArgs() }
    private val binding by viewBinding(CertificateSwitcherItemBinding::inflate)
    private val maskStatus by lazy {
        covpassDeps.certRepository.certs.value.getGroupedCertificates(
            args.certId,
        )?.maskStatusWrapper?.maskStatus
    }

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
        val combinedCovCertificate = groupedCertificate.certificates.find {
            it.covCertificate.dgcEntry.id == args.id
        } ?: return
        val covCertificate = combinedCovCertificate.covCertificate
        val certStatus = combinedCovCertificate.status

        launchWhenStarted {
            binding.certificateCard.qrCodeImage = generateQRCode(combinedCovCertificate.qrContent)
        }

        when (val dgcEntry = covCertificate.dgcEntry) {
            is Vaccination -> {
                when (dgcEntry.type) {
                    VaccinationCertType.VACCINATION_FULL_PROTECTION -> {
                        val vaccination = covCertificate.dgcEntry as Vaccination
                        binding.certificateCard.createCertificateSwitcherItemView(
                            certStatus,
                            getString(
                                R.string.certificates_overview_vaccination_certificate_message,
                                vaccination.doseNumber,
                                vaccination.totalSerialDoses,
                            ),
                            getString(
                                R.string.certificate_timestamp_months,
                                vaccination.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                    ?.toInstant()?.monthTillNow(),
                            ),
                            maskStatus,
                            if (maskStatus == MaskStatus.Required) {
                                R.drawable.main_cert_status_complete
                            } else {
                                R.drawable.main_cert_status_complete_green
                            },
                        )
                    }
                    VaccinationCertType.VACCINATION_COMPLETE -> {
                        val vaccination = covCertificate.dgcEntry as Vaccination
                        binding.certificateCard.createCertificateSwitcherItemView(
                            certStatus,
                            getString(
                                R.string.certificates_overview_vaccination_certificate_message,
                                vaccination.doseNumber,
                                vaccination.totalSerialDoses,
                            ),
                            getString(
                                R.string.certificate_timestamp_months,
                                vaccination.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                    ?.toInstant()?.monthTillNow(),
                            ),
                            maskStatus,
                            if (maskStatus == MaskStatus.Required) {
                                R.drawable.main_cert_status_incomplete
                            } else {
                                R.drawable.main_cert_status_incomplete_green
                            },
                        )
                    }
                    VaccinationCertType.VACCINATION_INCOMPLETE -> {
                        val vaccination = covCertificate.dgcEntry as Vaccination
                        binding.certificateCard.createCertificateSwitcherItemView(
                            certStatus,
                            getString(
                                R.string.certificates_overview_vaccination_certificate_message,
                                vaccination.doseNumber,
                                vaccination.totalSerialDoses,
                            ),
                            getString(
                                R.string.certificate_timestamp_months,
                                vaccination.occurrence?.atStartOfDay(ZoneId.systemDefault())
                                    ?.toInstant()?.monthTillNow(),
                            ),
                            maskStatus,
                            if (maskStatus == MaskStatus.Required) {
                                R.drawable.main_cert_status_incomplete
                            } else {
                                R.drawable.main_cert_status_incomplete_green
                            },
                        )
                    }
                }
            }
            is TestCert -> {
                val test = covCertificate.dgcEntry as TestCert
                binding.certificateCard.createCertificateSwitcherItemView(
                    certStatus,
                    if (test.testType == TestCert.PCR_TEST) {
                        getString(R.string.certificate_type_pcrtest)
                    } else {
                        getString(R.string.certificate_type_rapidtest)
                    },
                    getString(
                        R.string.certificate_timestamp_hours,
                        test.sampleCollection?.hoursTillNow(),
                    ),
                    maskStatus,
                    if (maskStatus == MaskStatus.Required) {
                        R.drawable.main_cert_test_blue
                    } else {
                        R.drawable.main_cert_test_green
                    },
                )
            }
            is Recovery -> {
                val recovery = covCertificate.dgcEntry as Recovery
                binding.certificateCard.createCertificateSwitcherItemView(
                    certStatus,
                    getString(R.string.certificate_type_recovery),
                    getString(
                        R.string.certificate_timestamp_months,
                        recovery.firstResult?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
                            ?.monthTillNow(),
                    ),
                    maskStatus,
                    if (maskStatus == MaskStatus.Required) {
                        R.drawable.main_cert_status_complete
                    } else {
                        R.drawable.main_cert_status_complete_green
                    },
                )
            }
            // .let{} to enforce exhaustiveness
        }.let {}

        binding.certificateCard.setOnCardClickListener {
            findNavigator().push(
                DetailFragmentNav(args.certId),
            )
        }
    }

    private suspend fun generateQRCode(qrContent: String): Bitmap {
        return dispatchers.default {
            BarcodeEncoder().encodeBitmap(
                qrContent,
                BarcodeFormat.QR_CODE,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.widthPixels,
                mapOf(EncodeHintType.MARGIN to 0),
            )
        }
    }
}
