package com.ibm.health.common.navigation.android

import android.app.Activity
import androidx.fragment.app.Fragment
import com.ibm.health.common.annotations.Abortable

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
