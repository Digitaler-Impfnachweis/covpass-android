/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.uielements

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ibm.health.common.android.utils.BaseRecyclerViewAdapter
import com.ibm.health.common.android.utils.BindingViewHolder
import de.rki.covpass.app.databinding.InfoElementListItemBinding
import de.rki.covpass.app.databinding.InfoElementWithListBinding
import de.rki.covpass.commonapp.R
import kotlin.properties.Delegates

public class InfoElementWithList @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(
    context,
    attrs,
    defStyleAttr,
) {
    private val binding: InfoElementWithListBinding =
        InfoElementWithListBinding.inflate(LayoutInflater.from(context))

    public var title: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoTitle.text = newValue
    }

    public var subtitle: String? by Delegates.observable(null) { _, _, newValue ->
        binding.infoSubtitle.text = newValue
        binding.infoSubtitle.isGone = newValue.isNullOrEmpty()
    }

    public var icon: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.infoIcon.setImageDrawable(newValue)
        binding.infoIcon.isVisible = newValue != null
    }

    public var elementColor: Drawable? by Delegates.observable(null) { _, _, newValue ->
        binding.root.background = newValue
    }

    public fun updateList(list: List<String>, parent: Fragment) {
        InfoElementListAdapter(list, parent).attachTo(binding.infoRecycler)
    }

    init {
        initView(attrs)
        addView(binding.root)
        binding.root.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initView(attrs: AttributeSet?) {
        attrs ?: return

        val attributeValues = context.obtainStyledAttributes(attrs, R.styleable.InfoElementWithList)
        with(attributeValues) {
            try {
                title = getString(R.styleable.InfoElement_title)
                subtitle = getString(R.styleable.InfoElement_subtitle)
                icon = getDrawable(R.styleable.InfoElement_icon)
                elementColor = getDrawable(R.styleable.InfoElement_elementColor)
            } finally {
                recycle()
            }
        }
    }
}

public class InfoElementListAdapter(
    private val items: List<String> = emptyList(),
    parent: Fragment,
) : BaseRecyclerViewAdapter<InfoElementListAdapter.InfoElementViewHolder>(parent) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): InfoElementViewHolder = InfoElementViewHolder(parent)

    override fun onBindViewHolder(holder: InfoElementViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int = items.size

    public class InfoElementViewHolder(parent: ViewGroup) :
        BindingViewHolder<InfoElementListItemBinding>(
            parent,
            InfoElementListItemBinding::inflate,
        ) {

        public fun onBind(text: String) {
            binding.consentInitializationNoteValue.text = text
        }
    }
}

public fun InfoElementWithList.setValues(
    title: String,
    subtitle: String? = null,
    iconRes: Int? = null,
    backgroundRes: Int? = null,
    list: List<String>,
    parent: Fragment,
) {
    this.title = title
    this.subtitle = subtitle
    this.icon = iconRes?.let { ContextCompat.getDrawable(context, it) }
    this.elementColor = backgroundRes?.let { ContextCompat.getDrawable(context, it) }
    this.updateList(list, parent)
}
