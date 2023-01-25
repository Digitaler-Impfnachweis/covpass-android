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
import de.rki.covpass.app.databinding.CertificateDataElementWithCheckboxBinding
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateOrEmpty
import de.rki.covpass.sdk.utils.formatDateTime
import de.rki.covpass.sdk.utils.toDeviceTimeZone
import kotlin.properties.Delegates

public class CertificateDataElementWithCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: CertificateDataElementWithCheckboxBinding =
        CertificateDataElementWithCheckboxBinding.inflate(LayoutInflater.from(context))

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

    init {
        addView(binding.root)
        binding.root.layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun isChecked(): Boolean = binding.checkContextCheckboxElementCheckbox.isChecked

    public fun changeCheckbox(check: Boolean) {
        binding.checkContextCheckboxElementCheckbox.isChecked = check
    }

    public fun showCertificateData(
        name: String,
        type: String,
        info: String,
        date: String,
    ) {
        this.name = name
        this.type = type
        this.info = info
        this.date = date
    }

    public fun showCertificate(covCertificate: CovCertificate) {
        name = covCertificate.fullName
        when (val dgcEntry = covCertificate.dgcEntry) {
            is Vaccination -> {
                type = getString(R.string.certificate_check_validity_vaccination)
                info = getString(
                    R.string.certificates_overview_vaccination_certificate_message,
                    dgcEntry.doseNumber,
                    dgcEntry.totalSerialDoses,
                )
                date = getString(
                    R.string.certificates_overview_vaccination_certificate_date,
                    dgcEntry.occurrence.formatDateOrEmpty(),
                )
            }
            is TestCert -> {
                type = getString(R.string.certificate_check_validity_test)
                info = if (dgcEntry.testType == TestCert.PCR_TEST) {
                    getString(R.string.test_certificate_detail_view_pcr_test_title)
                } else {
                    getString(R.string.test_certificate_detail_view_title)
                }
                date = getString(
                    R.string.certificates_overview_test_certificate_date,
                    dgcEntry.sampleCollection?.toDeviceTimeZone()?.formatDateTime() ?: "",
                )
            }
            is Recovery -> {
                type = getString(R.string.certificate_check_validity_recovery)
                info = getString(R.string.recovery_certificate_detail_view_title)
                date = getString(
                    R.string.certificates_overview_recovery_certificate_sample_date,
                    (covCertificate.dgcEntry as Recovery).firstResult?.formatDateOrEmpty() ?: "",
                )
            }
        }
    }
}
