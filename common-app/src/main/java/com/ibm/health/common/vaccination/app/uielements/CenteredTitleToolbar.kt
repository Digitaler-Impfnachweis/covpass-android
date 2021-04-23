package com.ibm.health.common.vaccination.app.uielements

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.TextViewCompat
import com.ibm.health.common.vaccination.app.R
import com.ibm.health.common.vaccination.app.utils.getScreenSize

/**
 * Custom [Toolbar] with a centered title instead of left alignment as the actual [Toolbar].
 */
public class CenteredTitleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(
    context,
    attrs,
    defStyleAttr
) {

    private var screenWidth: Int = 0
    private lateinit var title: TextView

    init {
        screenWidth = context.getScreenSize().x
        title = TextView(context)
        TextViewCompat.setTextAppearance(title, R.style.Header_OnBackground_Standard)
        addView(title)
    }

    /** @suppress */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        title.width.let { width ->
            title.left = screenWidth / 2 - width / 2
            title.right = title.left + width
        }
    }

    /** @suppress */
    override fun setTitle(resId: Int) {
        title.setText(resId)
        requestLayout()
    }

    /** @suppress */
    override fun setTitle(title: CharSequence?) {
        this.title.text = title
    }
}
