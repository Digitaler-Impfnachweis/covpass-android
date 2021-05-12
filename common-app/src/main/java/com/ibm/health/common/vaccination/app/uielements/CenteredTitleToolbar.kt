package com.ibm.health.common.vaccination.app.uielements

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.TextViewCompat
import com.ibm.health.common.vaccination.app.R

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

    private val title: TextView = TextView(context)

    init {
        TextViewCompat.setTextAppearance(title, R.style.Header_OnBackground_Standard)
        addView(title)
    }

    /** @suppress */
    override fun setTitle(resId: Int) {
        getCenteredTextView().setText(resId)
        requestLayout()
    }

    /** @suppress */
    override fun setTitle(title: CharSequence?) {
        getCenteredTextView().text = title
    }

    private fun getCenteredTextView(): TextView {
        return title.apply {
            gravity = Gravity.CENTER
            val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            layoutParams = lp
        }
    }
}
