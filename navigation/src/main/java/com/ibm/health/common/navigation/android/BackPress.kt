/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import android.app.Activity
import androidx.fragment.app.Fragment
import com.ibm.health.common.annotations.Abortable

/** Executes a back navigation action without triggering onBackPressed. */
public fun Fragment.triggerBackNavigation(skip: Int = 0) {
    require(skip >= 0) { "Parameter must be >= 0" }
    for (skipNavigators in skip..Int.MAX_VALUE) {
        try {
            if (findNavigator(skip = skipNavigators).pop()) {
                break
            }
        } catch (e: NoSuchElementInHierarchy) {
            requireActivity().finish()
            break
        }
    }
}

/** Executes a back button press. */
public fun Fragment.triggerBackPress() {
    requireActivity().onBackPressed()
}

/** Executes a back button press. */
public fun Activity.triggerBackPress() {
    onBackPressed()
}

/**
 * Interface for fragments wanting to handle the system back button press.
 */
public interface OnBackPressedNavigation {
    public fun onBackPressed(): Abortable
}
