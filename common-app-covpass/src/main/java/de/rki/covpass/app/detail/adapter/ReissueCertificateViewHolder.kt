package de.rki.covpass.app.detail.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.ReissueCertificateItemBinding
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.DGCEntryType
import de.rki.covpass.sdk.cert.models.RecoveryCertType
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.VaccinationCertType

public class ReissueCertificateViewHolder(
    parent: ViewGroup,
) : BindingViewHolder<ReissueCertificateItemBinding>(
    parent,
    ReissueCertificateItemBinding::inflate,
) {

    public fun bind(item: ReissueCertificateItem) {
        when (item.type) {
            VaccinationCertType.VACCINATION_COMPLETE,
            VaccinationCertType.VACCINATION_FULL_PROTECTION,
            -> {
                binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.info)
                when (item.certStatus) {
                    CertValidationResult.Valid -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_status_complete)
                    }
                    CertValidationResult.ExpiryPeriod -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                    }
                    CertValidationResult.Expired,
                    CertValidationResult.Invalid,
                    CertValidationResult.Revoked,
                    -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                        binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                    }
                }
            }
            VaccinationCertType.VACCINATION_INCOMPLETE -> {
                binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.info20)
                when (item.certStatus) {
                    CertValidationResult.Valid -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_status_incomplete)
                    }
                    CertValidationResult.ExpiryPeriod -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                    }
                    CertValidationResult.Expired,
                    CertValidationResult.Invalid,
                    CertValidationResult.Revoked,
                    -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                        binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                    }
                }
            }
            TestCertType.NEGATIVE_PCR_TEST,
            TestCertType.NEGATIVE_ANTIGEN_TEST,
            -> {
                binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(
                    R.color.test_certificate_background,
                )
                when (item.certStatus) {
                    CertValidationResult.Valid -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_test)
                    }
                    CertValidationResult.ExpiryPeriod -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                    }
                    CertValidationResult.Expired,
                    CertValidationResult.Invalid,
                    CertValidationResult.Revoked,
                    -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                        binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                    }
                }
            }
            RecoveryCertType.RECOVERY -> {
                binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.infoDark)
                when (item.certStatus) {
                    CertValidationResult.Valid -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_recovery)
                    }
                    CertValidationResult.ExpiryPeriod -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                    }
                    CertValidationResult.Expired,
                    CertValidationResult.Invalid,
                    CertValidationResult.Revoked,
                    -> {
                        binding.reissueCertificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                        binding.reissueCertificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                    }
                }
            }
            TestCertType.POSITIVE_PCR_TEST,
            TestCertType.POSITIVE_ANTIGEN_TEST,
            -> return
            // .let{} to enforce exhaustiveness
        }.let {}
        binding.reissueCertificateTypeIcon.setTint(
            if (item.isExpiredInvalidOrRevoked()) {
                R.color.backgroundSecondary50
            } else {
                when (item.type) {
                    VaccinationCertType.VACCINATION_INCOMPLETE -> R.color.info
                    else -> R.color.backgroundSecondary
                }
            },
        )
        binding.reissueCertificateItemTitle.text = item.fullname
        binding.reissueCertificateItemSubtitle.text = item.certificateType
        binding.reissueCertificateItemStatus.text = item.certificateProgress
        binding.reissueCertificateItemDate.text = item.date
        binding.reissueCertificateItemActualTitle.isVisible = item.isFirst
    }
}

public data class ReissueCertificateItem(
    val type: DGCEntryType,
    val fullname: String,
    val certificateType: String,
    val certificateProgress: String,
    val date: String,
    val isFirst: Boolean = false,
    val certStatus: CertValidationResult = CertValidationResult.Valid,
)

private fun ReissueCertificateItem.isExpiredInvalidOrRevoked() =
    certStatus == CertValidationResult.Expired ||
        certStatus == CertValidationResult.Invalid ||
        certStatus == CertValidationResult.Revoked
