/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.view.View
import androidx.fragment.app.FragmentManager

public fun FragmentManager.setupForAccessibility() {
    addOnBackStackChangedListener {
        val lastFragmentWithView = fragments.last { it.view != null }
        for (fragment in fragments) {
            if (fragment == lastFragmentWithView) {
                fragment.view?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            } else {
                fragment.view?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
        }
    }
}
