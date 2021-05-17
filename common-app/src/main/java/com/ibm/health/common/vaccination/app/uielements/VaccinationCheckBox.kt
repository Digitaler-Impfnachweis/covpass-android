package com.ibm.health.common.vaccination.app.uielements

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.ibm.health.common.android.utils.getSpanned
import com.ibm.health.common.vaccination.app.databinding.CustomCheckboxBinding
import com.ibm.health.common.vaccination.app.utils.stripUnderlines

/**
 * A custom view with a checkbox on the left and a text on the right.
 */
public class VaccinationCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: CustomCheckboxBinding =
        CustomCheckboxBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        orientation = HORIZONTAL
        binding.checkboxText.movementMethod = LinkMovementMethod.getInstance()
    }

    public fun setText(text: Int) {
        binding.checkboxText.setText(text)
        binding.checkboxText.stripUnderlines()
    }

    public fun setLinkedText(text: Int, link: Int) {
        binding.checkboxText.text = getSpanned(context.getString(text), context.getString(link))
        binding.checkboxText.stripUnderlines()
    }

    public fun isChecked(): Boolean = binding.checkbox.isChecked

    public fun addOnCheckListener(listener: CompoundButton.OnCheckedChangeListener) {
        binding.checkbox.setOnCheckedChangeListener(listener)
    }
}
