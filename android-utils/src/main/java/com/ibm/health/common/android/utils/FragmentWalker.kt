/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

public fun <T : Any> FragmentManager.findFragments(recursive: Boolean = true, match: (Fragment) -> T?): Sequence<T> =
    sequence {
        for (fragment in fragments.reversed()) {
            match(fragment)?.let {
                yield(it)
            }
            if (recursive) {
                yieldAll(fragment.childFragmentManager.findFragments(recursive = recursive, match = match))
            }
        }
    }
