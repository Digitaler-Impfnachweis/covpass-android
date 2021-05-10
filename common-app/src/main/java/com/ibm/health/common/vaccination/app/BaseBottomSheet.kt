package com.ibm.health.common.vaccination.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.validUntil
import com.ibm.health.common.navigation.android.SheetPaneNavigation
import com.ibm.health.common.navigation.android.triggerBackPress
import com.ibm.health.common.vaccination.app.databinding.BottomSheetViewBinding

/** Common base bottom sheet. */
public abstract class BaseBottomSheet : BaseFragment(), SheetPaneNavigation {

    public abstract val buttonTextRes: Int
    public open val heightLayoutParams: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    protected var bottomSheetBinding: BottomSheetViewBinding by validUntil(::onDestroyView)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        bottomSheetBinding = BottomSheetViewBinding.inflate(inflater, container, false)
        // Add the bottom sheet content dynamically into the container.
        // To support view bindings, we must call super.onCreateView, as view inflation
        // is hooked in [BaseHookedFragment].
        // If the super implementation returns a non-null view, it is added as a child into the
        // content area.
        super.onCreateView(inflater, bottomSheetBinding.bottomSheetContent, savedInstanceState)?.let {
            bottomSheetBinding.bottomSheetContent.addView(it)
        }

        bottomSheetBinding.bottomSheet.layoutParams.height = heightLayoutParams
        bottomSheetBinding.bottomSheetClose.setOnClickListener { triggerBackPress() }
        bottomSheetBinding.bottomSheetActionButton.text = getString(buttonTextRes)
        bottomSheetBinding.bottomSheetActionButton.setOnClickListener {
            onActionButtonClicked()
        }

        // Adds padding to the content view equal to bottomSheetBottomView.height
        bottomSheetBinding.bottomSheetBottomView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val bottomViewHeight = height
                    try {
                        bottomSheetBinding
                    } catch (e: IllegalStateException) {
                        return
                    }
                    bottomSheetBinding.bottomSheetContent.getChildAt(0)?.run {
                        setPadding(paddingLeft, paddingTop, paddingRight, bottomViewHeight)
                    }
                }
            })
        }

        return bottomSheetBinding.root
    }

    protected abstract fun onActionButtonClicked()

    override fun onClickOutside() {
        super.onClickOutside()
        triggerBackPress()
    }
}
