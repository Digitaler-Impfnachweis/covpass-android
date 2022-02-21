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
import de.rki.covpass.commonapp.databinding.CheckContextCheckboxBinding
import kotlin.properties.Delegates

public class CheckContextCheckboxElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val binding: CheckContextCheckboxBinding =
        CheckContextCheckboxBinding.inflate(LayoutInflater.from(context))

    private var title: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.checkContextCheckboxElementTitle.setText(newValue)
        }
        binding.checkContextCheckboxElementTitle.isVisible = newValue != null
    }

    private var subtitle: Int? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) {
            binding.checkContextCheckboxElementSubtitle.setText(newValue)
        }
        binding.checkContextCheckboxElementSubtitle.isVisible = newValue != null
    }

    private var isChecked: Boolean by Delegates.observable(false) { _, _, newValue ->
        binding.checkContextCheckboxElementCheckbox.isChecked = newValue
    }

    init {
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    public fun updateValues(title: Int?, subtitle: Int?) {
        this.title = title
        this.subtitle = subtitle
    }

    public fun updateCheckbox(isChecked: Boolean) {
        this.isChecked = isChecked
    }

    @JvmName("isCheckboxChecked")
    public fun isChecked(): Boolean = binding.checkContextCheckboxElementCheckbox.isChecked
}
