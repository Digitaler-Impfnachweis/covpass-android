/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.uielements

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.TextViewWithHintElementBinding
import kotlin.properties.Delegates

public class TextViewWithHint @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: TextViewWithHintElementBinding =
        TextViewWithHintElementBinding.inflate(LayoutInflater.from(context))

    private var text: String? by Delegates.observable(null) { _, _, newValue ->
        binding.textViewValue.text = newValue
    }

    private var description: String? by Delegates.observable(null) { _, _, newValue ->
        binding.textViewDescription.text = newValue
        binding.textViewDescription.isGone = newValue.isNullOrEmpty()
    }

    private var hint: String? by Delegates.observable(null) { _, _, newValue ->
        binding.textViewHint.text = newValue
        binding.textViewHint.isGone = newValue.isNullOrEmpty()
    }

    private var icon: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.textViewIcon.setImageDrawable(newValue)
        binding.textViewIcon.isVisible = newValue != null
    }

    public var marginVertical: Int by Delegates.observable(R.dimen.grid_three) { _, _, newValue ->
        val layoutParams = (binding.textViewLayout.layoutParams as? MarginLayoutParams)
        layoutParams?.setMargins(
            layoutParams.topMargin,
            resources.getDimension(newValue).toInt(),
            layoutParams.bottomMargin,
            resources.getDimension(newValue).toInt(),
        )
        binding.textViewLayout.layoutParams = layoutParams
    }

    init {
        initView(attrs)
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initView(attrs: AttributeSet?) {
        attrs ?: return

        val attributeValues = context.obtainStyledAttributes(attrs, R.styleable.TextViewWithHint)
        with(attributeValues) {
            try {
                text = getString(R.styleable.TextViewWithHint_text)
                hint = getString(R.styleable.TextViewWithHint_hint)
                icon = getDrawable(R.styleable.TextViewWithHint_icon)
                description = getString(R.styleable.TextViewWithHint_description)
            } finally {
                recycle()
            }
        }
    }

    public fun setValues(
        text: String,
        hint: String? = null,
        icon: Drawable? = null,
    ) {
        this.text = text
        this.hint = hint
        this.icon = icon
    }

    public fun updateText(text: String) {
        this.text = text
    }

    public fun updateDescription(description: String?) {
        this.description = description
    }
}
