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
import de.rki.covpass.checkapp.R
import de.rki.covpass.checkapp.databinding.ValidationResultInfoBinding
import kotlin.properties.Delegates

public class ValidationResultInfoElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: ValidationResultInfoBinding =
        ValidationResultInfoBinding.inflate(LayoutInflater.from(context))

    private var icon: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultInfoImage.setImageResource(newValue)
        }
        binding.resultInfoImage.isVisible = newValue != null
    }

    private var title: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultInfoTitle.text = newValue
        }
        binding.resultInfoTitle.isVisible = newValue != null
    }
    private var subtitle: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultInfoSubtitle.text = newValue
        }
        binding.resultInfoSubtitle.isVisible = newValue != null
    }
    private var text: String? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.resultInfoText.text = newValue
        }
        binding.resultInfoText.isVisible = newValue != null
    }

    private var warning: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.resultInfoCardview.setBackgroundResource(
            if (newValue) {
                R.drawable.result_2g_info_element_warning_background
            } else {
                R.drawable.result_2g_element_background
            },
        )
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun showInfo(icon: Int?, title: String?, subtitle: String?, text: String?, warning: Boolean = false) {
        this.icon = icon
        this.title = title
        this.subtitle = subtitle
        this.text = text
        this.warning = warning
    }
}
