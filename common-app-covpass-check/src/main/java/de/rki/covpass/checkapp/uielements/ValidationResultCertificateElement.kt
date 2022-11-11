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
import de.rki.covpass.checkapp.databinding.ValidationResultCertificateBinding
import kotlin.properties.Delegates

public class ValidationResultCertificateElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: ValidationResultCertificateBinding =
        ValidationResultCertificateBinding.inflate(LayoutInflater.from(context))

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

    private var resultCertificateNote: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultCertificateNote.text = newValue
        }
        binding.resultCertificateNote.isVisible = newValue != null
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun showCertificate(icon: Int?, title: String?, note: String?) {
        this.icon = icon
        this.title = title
        this.resultCertificateNote = note
        binding.resultCertificateCardview.isClickable = false
        binding.resultCertificateCardview.isFocusable = false
    }
}
