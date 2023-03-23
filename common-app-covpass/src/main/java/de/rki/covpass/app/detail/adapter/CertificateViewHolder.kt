package de.rki.covpass.app.detail.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateItemBinding
import de.rki.covpass.app.detail.DetailClickListener
import de.rki.covpass.sdk.cert.models.CertValidationResult
import de.rki.covpass.sdk.cert.models.RecoveryCertType
import de.rki.covpass.sdk.cert.models.TestCertType
import de.rki.covpass.sdk.cert.models.VaccinationCertType

public class CertificateViewHolder(
    parent: ViewGroup,
    private val listener: DetailClickListener? = null,
) : BaseViewHolder<CertificateItemBinding>(parent, CertificateItemBinding::inflate) {

    override fun onItemBind(item: DetailItem) {
        (item as DetailItem.Certificate).let { cert ->
            when (cert.type) {
                VaccinationCertType.VACCINATION_COMPLETE,
                VaccinationCertType.VACCINATION_FULL_PROTECTION,
                -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.info
                        } else {
                            R.color.backgroundSecondary20
                        },
                    )
                    binding.certificateItemReissueTitle.isVisible = cert.showReissueTitle
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_complete)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid,
                        CertValidationResult.Revoked,
                        -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                VaccinationCertType.VACCINATION_INCOMPLETE -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.info20
                        } else {
                            R.color.backgroundSecondary20
                        },
                    )
                    binding.certificateItemReissueTitle.isVisible = cert.showReissueTitle
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_status_incomplete)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid,
                        CertValidationResult.Revoked,
                        -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                TestCertType.NEGATIVE_PCR_TEST,
                TestCertType.NEGATIVE_ANTIGEN_TEST,
                -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.test_certificate_background
                        } else {
                            R.color.backgroundSecondary20
                        },
                    )
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_test)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid,
                        CertValidationResult.Revoked,
                        -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                RecoveryCertType.RECOVERY -> {
                    binding.certificateStatusLayout.setLayoutBackgroundColor(
                        if (cert.isActual) {
                            R.color.infoDark
                        } else {
                            R.color.backgroundSecondary20
                        },
                    )
                    binding.certificateItemReissueTitle.isVisible = cert.showReissueTitle
                    when (cert.certStatus) {
                        CertValidationResult.Valid -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_recovery)
                        }
                        CertValidationResult.ExpiryPeriod -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expiry_period)
                        }
                        CertValidationResult.Expired,
                        CertValidationResult.Invalid,
                        CertValidationResult.Revoked,
                        -> {
                            binding.certificateTypeIcon.setImageResource(R.drawable.main_cert_expired)
                            binding.certificateStatusLayout.setLayoutBackgroundColor(R.color.backgroundSecondary20)
                        }
                    }
                }
                TestCertType.POSITIVE_PCR_TEST,
                TestCertType.POSITIVE_ANTIGEN_TEST,
                -> return
                // .let{} to enforce exhaustiveness
            }.let {}
            binding.certificateTypeIcon.setTint(
                if (cert.isActual && cert.isExpiredInvalidOrRevoked()) {
                    R.color.backgroundSecondary50
                } else if (cert.isActual) {
                    when (cert.type) {
                        VaccinationCertType.VACCINATION_INCOMPLETE -> R.color.info
                        else -> R.color.backgroundSecondary
                    }
                } else {
                    R.color.backgroundSecondary50
                },
            )
            binding.certificateItemActualTitle.isVisible = cert.isActualAndValid()
            binding.certificateItemTitle.text = cert.title
            binding.certificateItemSubtitle.text = cert.subtitle
            binding.certificateItemDate.text = cert.date
            binding.certificateItemArrow.isVisible = listener != null
            binding.root.setOnClickListener {
                listener?.onCovCertificateClicked(cert.id, cert.type)
            }

            when (cert.certStatus) {
                CertValidationResult.Valid -> {
                    binding.certificateExpiryInfo.isGone = true
                }
                CertValidationResult.ExpiryPeriod -> {
                    binding.certificateExpiryInfo.isGone = cert.showReissueTitle
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_expires_soon_certificate_note)
                }
                CertValidationResult.Expired -> {
                    binding.certificateExpiryInfo.isGone = cert.showReissueTitle
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_expired_certificate_note)
                }
                CertValidationResult.Invalid,
                CertValidationResult.Revoked,
                -> {
                    binding.certificateExpiryInfo.isGone = cert.showReissueTitle
                    binding.certificateExpiryInfo.text =
                        getString(R.string.certificates_overview_invalid_certificate_note)
                }
            }
        }
    }
}

private fun DetailItem.Certificate.isActualAndValid() =
    when (certStatus) {
        CertValidationResult.Expired,
        CertValidationResult.Revoked,
        CertValidationResult.Invalid,
        -> false
        CertValidationResult.Valid,
        CertValidationResult.ExpiryPeriod,
        -> isActual
    }

private fun DetailItem.Certificate.isExpiredInvalidOrRevoked() =
    certStatus == CertValidationResult.Expired ||
        certStatus == CertValidationResult.Invalid ||
        certStatus == CertValidationResult.Revoked
