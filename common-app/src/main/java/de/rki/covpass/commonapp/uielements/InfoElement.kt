/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.uielements

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.ibm.health.common.android.utils.getSpanned
import de.rki.covpass.commonapp.R
import de.rki.covpass.commonapp.databinding.InfoElementBinding
import de.rki.covpass.commonapp.utils.stripUnderlines
import kotlin.properties.Delegates

public class InfoElement @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: InfoElementBinding = InfoElementBinding.inflate(LayoutInflater.from(context))

    public var title: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoTitle.text = newValue
    }

    public var subtitle: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoSubtitle.text = newValue
        binding.infoSubtitle.isGone = newValue.isNullOrEmpty()
    }

    public var subtitleContentDescription: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoSubtitle.contentDescription = newValue
    }

    public var subtitleStyle: Int by Delegates.observable(R.style.DefaultText_OnBackground70) { _, _, newValue ->
        binding.infoSubtitle.setTextAppearance(newValue)
    }

    public var subtitleTopMarginDimenRes: Int by Delegates.observable(R.dimen.grid_zero) { _, _, newValue ->
        val marginLayoutParams = binding.infoSubtitle.layoutParams as MarginLayoutParams
        marginLayoutParams.topMargin = resources.getDimensionPixelSize(newValue)
        binding.infoSubtitle.requestLayout()
    }

    public var description: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoDescription.apply {
            text = getSpanned(newValue ?: "")
            movementMethod = LinkMovementMethod.getInstance()
            stripUnderlines()
        }
        binding.infoDescription.isGone = newValue.isNullOrEmpty()
    }

    public var descriptionContentDescription: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoDescription.contentDescription = newValue
    }

    public var descriptionLink: OnClickListener? by Delegates.observable(null) { _, _, newValue ->
        binding.infoDescription.setOnClickListener(newValue)
    }

    public var titleStyle: Int by Delegates.observable(R.style.Header_OnBackground_Small) { _, _, newValue ->
        binding.infoTitle.setTextAppearance(newValue)
    }

    public var descriptionStyle: Int by Delegates.observable(R.style.DefaultText_OnBackground) { _, _, newValue ->
        binding.infoDescription.setTextAppearance(newValue)
    }

    public var descriptionTopMarginDimenRes: Int by Delegates.observable(R.dimen.grid_one) { _, _, newValue ->
        val marginLayoutParams = binding.infoDescription.layoutParams as MarginLayoutParams
        marginLayoutParams.topMargin = resources.getDimensionPixelSize(newValue)
        binding.infoDescription.requestLayout()
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
    description: String? = null,
) {
    this.title = title
    this.subtitle = subtitle
    this.description = description
}

public fun InfoElement.showWarning(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null,
    subtitleTopMarginDimenRes: Int? = null,
    descriptionTopMarginDimenRes: Int? = null,
    subtitleStyle: Int? = null,
    descriptionStyle: Int? = null,
    subtitleContentDescription: String? = null,
    descriptionContentDescription: String? = null,
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.warning_background)
    if (subtitleTopMarginDimenRes != null) {
        this.subtitleTopMarginDimenRes = subtitleTopMarginDimenRes
    }
    if (descriptionTopMarginDimenRes != null) {
        this.descriptionTopMarginDimenRes = descriptionTopMarginDimenRes
    }
    if (subtitleStyle != null) {
        this.subtitleStyle = subtitleStyle
    }
    if (descriptionStyle != null) {
        this.descriptionStyle = descriptionStyle
    }
    if (subtitleContentDescription != null) {
        this.subtitleContentDescription = subtitleContentDescription
    }
    if (descriptionContentDescription != null) {
        this.descriptionContentDescription = descriptionContentDescription
    }
}

public fun InfoElement.showError(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null,
    subtitleContentDescription: String? = null,
    descriptionContentDescription: String? = null,
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.error_background)
    if (subtitleContentDescription != null) {
        this.subtitleContentDescription = subtitleContentDescription
    }
    if (descriptionContentDescription != null) {
        this.descriptionContentDescription = descriptionContentDescription
    }
}

public fun InfoElement.showSuccess(
    title: String,
    subtitle: String? = null,
    description: String? = null,
    iconRes: Int? = null,
    subtitleContentDescription: String? = null,
    descriptionContentDescription: String? = null,
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.success_background)
    if (subtitleContentDescription != null) {
        this.subtitleContentDescription = subtitleContentDescription
    }
    if (descriptionContentDescription != null) {
        this.descriptionContentDescription = descriptionContentDescription
    }
}

public fun InfoElement.showInfo(
    title: String,
    subtitle: String? = null,
    subtitleStyle: Int? = null,
    description: String? = null,
    iconRes: Int? = null,
    descriptionLink: View.OnClickListener? = null,
    descriptionStyle: Int? = null,
    subtitleTopMarginDimenRes: Int? = null,
    descriptionTopMarginDimenRes: Int? = null,
    subtitleContentDescription: String? = null,
    descriptionContentDescription: String? = null,
    titleStyle: Int? = null,
) {
    setValues(title, subtitle, description)
    icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    elementColor = ContextCompat.getDrawable(context, R.drawable.info_background)
    this.descriptionLink = descriptionLink
    if (subtitleStyle != null) {
        this.subtitleStyle = subtitleStyle
    }
    if (descriptionStyle != null) {
        this.descriptionStyle = descriptionStyle
    }
    if (titleStyle != null) {
        this.titleStyle = titleStyle
    }
    if (subtitleTopMarginDimenRes != null) {
        this.subtitleTopMarginDimenRes = subtitleTopMarginDimenRes
    }
    if (descriptionTopMarginDimenRes != null) {
        this.descriptionTopMarginDimenRes = descriptionTopMarginDimenRes
    }
    if (subtitleContentDescription != null) {
        this.subtitleContentDescription = subtitleContentDescription
    }
    if (descriptionContentDescription != null) {
        this.descriptionContentDescription = descriptionContentDescription
    }
}
