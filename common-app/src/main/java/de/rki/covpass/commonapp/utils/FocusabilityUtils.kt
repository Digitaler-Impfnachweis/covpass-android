/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

public fun Fragment.updateFragmentsFocusability() {
    val fragments = parentFragmentManager.fragments
    if (fragments.isNotEmpty()) {
        fragments.lastOrNull()?.apply { updateFocusabilityWhenViewIsReady(true) } ?: return
        fragments.dropLast(1).forEach {
            it.updateFocusabilityWhenViewIsReady(false)
        }
    }
}

private fun Fragment.updateFocusabilityWhenViewIsReady(isEnabled: Boolean) {
    view?.setAccessibilityFocusEnabled(isEnabled) ?: run {
        lifecycleScope.launchWhenStarted {
            view?.setAccessibilityFocusEnabled(isEnabled)
        }
    }
}

/**
 * Enables or disables all accessibility access on the view and its descendants. This is supposed to
 * be used on the root of an activity or fragment and both refer to their ui root as View as opposed
 * to ViewGroup. Hence this extension is also based on View but includes a safe cast to ViewGroup
 * to deal with descendant focusability
 */
public fun View.setAccessibilityFocusEnabled(isEnabled: Boolean) {
    if (isEnabled) {
        (this as? ViewGroup)?.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        importantForAccessibility = ViewGroup.IMPORTANT_FOR_ACCESSIBILITY_AUTO
    } else {
        (this as? ViewGroup)?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        importantForAccessibility = ViewGroup.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }
}
