/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.uielements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getString
import de.rki.covpass.checkapp.databinding.ValidationResult2gCertificateBinding
import kotlin.properties.Delegates

public class ValidationResult2gCertificateElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val binding: ValidationResult2gCertificateBinding =
        ValidationResult2gCertificateBinding.inflate(LayoutInflater.from(context))

    private var icon: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateIcon.setImageResource(newValue)
        }
        binding.resultCertificateIcon.isVisible = newValue != null
    }

    private var title: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateTitle.text = newValue
        }
        binding.resultCertificateTitle.isVisible = newValue != null
    }

    private var validFromText: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateValidFrom.text = newValue
        }
        binding.resultCertificateValidFrom.isVisible = newValue != null
    }
    private var textNotValidated: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateTextNotValidated.setText(newValue)
        }
        binding.resultCertificateTextNotValidated.isVisible = newValue != null
    }
    private var buttonClickListener: OnClickListener? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateCardview.setOnClickListener(newValue)
        }
        binding.resultCertificateButtonDetails.isVisible = newValue != null
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun showValidCertificate(icon: Int?, title: String?, validFromText: String?) {
        this.icon = icon
        this.title = title
        this.validFromText = validFromText
        binding.resultCertificateCardview.isClickable = false
    }

    public fun showEmptyCertificate(icon: Int?, title: Int?, textNotValidated: Int?) {
        this.icon = icon
        this.title = title?.let { getString(it) }
        this.textNotValidated = textNotValidated
        binding.resultCertificateCardview.isClickable = false
    }

    public fun showInvalidCertificate(icon: Int?, title: Int?, detailsClickListener: OnClickListener?) {
        this.icon = icon
        this.title = title?.let { getString(it) }
        this.buttonClickListener = detailsClickListener
    }
}
