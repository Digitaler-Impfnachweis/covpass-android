package de.rki.covpass.app.uielements

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getSpanned
import de.rki.covpass.app.R
import de.rki.covpass.app.databinding.InfoElementBinding
import de.rki.covpass.commonapp.utils.stripUnderlines
import kotlin.properties.Delegates

public class InfoElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr
) {
    private val binding: InfoElementBinding = InfoElementBinding.inflate(LayoutInflater.from(context))

    public var title: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoTitle.text = newValue
    }

    public var subtitle: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoSubtitle.text = newValue
        binding.infoSubtitle.isGone = newValue.isNullOrEmpty()
    }

    public var description: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoDescription.apply {
            text = getSpanned(newValue ?: "")
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.infoDescription.isGone = newValue.isNullOrEmpty()
    }

    public var icon: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.infoIcon.setImageDrawable(newValue)
        binding.infoIcon.isVisible = newValue != null
    }

    public var elementColor: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.root.background = newValue
    }

    init {
        initView(attrs)
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initView(attrs: AttributeSet?) {
        attrs ?: return

        val attributeValues = context.obtainStyledAttributes(attrs, R.styleable.InfoElement)
        with(attributeValues) {
            try {
                title = getString(R.styleable.InfoElement_title)
                subtitle = getString(R.styleable.InfoElement_subtitle)
                description = getString(R.styleable.InfoElement_description)
                icon = getDrawable(R.styleable.InfoElement_icon)
                elementColor = getDrawable(R.styleable.InfoElement_elementColor)
            } finally {
                recycle()
            }
        }
    }
}

private fun InfoElement.setValues(
    title: String,
    subtitle: String? = null,
    description: String? = null
) {
    this.title = title
    this.subtitle = subtitle
    this.description = description
}

public fun InfoElement.showWarning(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.warning_background)
}

public fun InfoElement.showError(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.error_background)
}

public fun InfoElement.showSuccess(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.success_background)
}

public fun InfoElement.showInfo(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.info_background)
}
