/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.uielements

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import de.rki.covpass.commonapp.databinding.ExpertModeToggleBinding
import kotlin.properties.Delegates

public class ExpertModeToggleElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: ExpertModeToggleBinding =
        ExpertModeToggleBinding.inflate(LayoutInflater.from(context))

    private var title: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.expertModeToggleElementTitle.setText(newValue)
        }
        binding.expertModeToggleElementTitle.isVisible = newValue != null
    }

    private var isChecked: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.expertModeToggleElement.isChecked = newValue
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun updateTitle(title: Int?) {
        this.title = title
    }

    public fun updateToggle(isChecked: Boolean) {
        this.isChecked = isChecked
    }

    @JvmName("isCheckboxChecked")
    public fun isChecked(): Boolean = binding.expertModeToggleElement.isChecked
}
