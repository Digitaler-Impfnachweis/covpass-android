/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.StringRes
import com.ensody.reactivestate.android.onDestroyView
import com.ensody.reactivestate.validUntil
import com.ibm.health.common.navigation.android.SheetPaneNavigation
import com.ibm.health.common.navigation.android.triggerBackPress
import de.rki.covpass.commonapp.databinding.BottomSheetViewBinding

/** Common base bottom sheet. */
public abstract class BaseBottomSheet : BaseFragment(), SheetPaneNavigation {

    @StringRes
    public open val buttonTextRes: Int? = null
    public open val heightLayoutParams: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    protected var bottomSheetBinding: BottomSheetViewBinding by validUntil(::onDestroyView)
    public open val announcementAccessibilityRes: Int? = null

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
        bottomSheetBinding.bottomSheetClose.setOnClickListener { onCloseButtonClicked() }
        buttonTextRes?.let { bottomSheetBinding.bottomSheetActionButton.text = getString(it) }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        announcementAccessibilityRes?.let { bottomSheetBinding.bottomSheet.announceForAccessibility(getString(it)) }
    }

    public open fun onCloseButtonClicked() {
        triggerBackPress()
    }

    protected abstract fun onActionButtonClicked()

    override fun onClickOutside() {
        super.onClickOutside()
        triggerBackPress()
    }
}
