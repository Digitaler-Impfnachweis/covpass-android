/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.uielements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.CertificateDataElementBinding
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlin.properties.Delegates

public class CertificateDataElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val binding: CertificateDataElementBinding =
        CertificateDataElementBinding.inflate(LayoutInflater.from(context))

    private var name: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateDataElementName.text = newValue
        binding.certificateDataElementName.isVisible = newValue != null
    }

    private var type: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateDataElementType.text = newValue
        binding.certificateDataElementType.isVisible = newValue != null
    }

    private var info: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateDataElementInfo.text = newValue
        binding.certificateDataElementInfo.isVisible = newValue != null
    }

    private var date: String? by Delegates.observable(null) { _, _, newValue ->
        binding.certificateDataElementDate.text = newValue
        binding.certificateDataElementDate.isVisible = newValue != null
    }

    private var arrow: Boolean by Delegates.observable(true) { _, _, newValue ->
        binding.certificateDataElementArrow.isVisible = newValue
    }

    private var typeBackground: Int by Delegates.observable(R.color.backgroundSecondary20) { _, _, newValue ->
        binding.certificateDataElementLayout.setBackgroundResource(newValue)
    }

    private var typeIcon: Int by Delegates.observable(R.drawable.validation_passed) { _, _, newValue ->
        binding.certificateDataElementTypeIcon.setImageResource(newValue)
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        binding.certificateDataElementArrow.isVisible = false
    }

    public fun showCertificateData(
        name: String,
        type: String,
        info: String,
        date: String,
        arrow: Boolean,
        certificateTypeBackground: Int,
        certificateTypeIcon: Int,
    ) {
        this.name = name
        this.type = type
        this.info = info
        this.date = date
        this.arrow = arrow
        this.typeBackground = certificateTypeBackground
        this.typeIcon = certificateTypeIcon
    }

    public fun showCertificate(covCertificate: CovCertificate, isOldCertificate: Boolean = false) {
        name = covCertificate.fullName
        when (val dgcEntry = covCertificate.dgcEntry) {
            is Vaccination -> {
                if (dgcEntry.isComplete) {
                    typeBackground = if (isOldCertificate) {
                        R.color.onBrandBase50
                    } else {
                        R.color.info
                    }
                    typeIcon = R.drawable.main_cert_status_complete_white
                } else {
                    typeBackground = R.color.info20
                    typeIcon = R.drawable.main_cert_status_incomplete
                }
                type = getString(R.string.certificate_check_validity_vaccination)
                info = getString(
                    R.string.certificates_overview_vaccination_certificate_message,
                    dgcEntry.doseNumber,
                    dgcEntry.totalSerialDoses
                )
                date = getString(
                    R.string.certificates_overview_vaccination_certificate_date,
                    dgcEntry.validDate.formatDateOrEmpty()
                )
            }
            is TestCert -> {
                typeBackground = R.color.test_certificate_background
                type = getString(R.string.certificate_check_validity_test)
                typeIcon = R.drawable.main_cert_test_white
                info = if (dgcEntry.testType == TestCert.PCR_TEST) {
                    getString(R.string.test_certificate_detail_view_pcr_test_title)
                } else {
                    getString(R.string.test_certificate_detail_view_title)
                }
                date = getString(
                    R.string.certificates_overview_test_certificate_date,
                    dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: ""
                )
            }
            is Recovery -> {
                typeBackground = R.color.info90
                type = getString(R.string.certificate_check_validity_recovery)
                typeIcon = R.drawable.main_cert_status_complete_white
                info = getString(R.string.recovery_certificate_detail_view_title)
                date = getString(
                    R.string.certificates_overview_recovery_certificate_valid_until_date,
                    covCertificate.validUntil?.formatDateOrEmpty() ?: ""
                )
            }
        }
    }
}
