/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.uielements

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import de.rki.covpass.app.R

public class TripleStateCheckBox : AppCompatCheckBox {
    private var state = 0

    public constructor(context: Context) : super(context) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        state = UNCHECKED
        updateBtn()
    }

    private fun updateBtn() {
        val buttonDrawable = when (state) {
            INDETERMINATE -> R.drawable.checkbox_indeterminate
            UNCHECKED -> R.drawable.checkbox_unchecked
            CHECKED -> R.drawable.checkbox_checked
            else -> R.drawable.checkbox_unchecked
        }
        setButtonDrawable(buttonDrawable)
    }

    public fun getState(): Int = state

    public fun setState(state: Int) {
        this.state = state
        updateBtn()
    }

    public companion object {
        public const val UNCHECKED: Int = 0
        public const val INDETERMINATE: Int = 1
        public const val CHECKED: Int = 2
    }
}
