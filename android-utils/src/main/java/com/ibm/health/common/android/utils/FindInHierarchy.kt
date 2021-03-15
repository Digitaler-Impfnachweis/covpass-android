package com.ibm.health.common.android.utils

import androidx.fragment.app.Fragment

/**
 * Searches from the current fragment upwards to parents and then the activity until [action] returns a non-null result.
 *
 * @return The non-null return value of [action].
 * @throws [NoSuchElementInHierarchy] if nothing was found.
 *
 * @param skip How many results to skip. Defaults to 0 (taking first result).
 */
public fun <T> Fragment.findInHierarchy(skip: Int = 0, action: (Any) -> T?): T {
    var fragment: Fragment? = this
    var skipCount = skip
    while (fragment != null) {
        val result = action(fragment)
        if (result != null) {
            if (skipCount == 0) {
                return result
            }
            skipCount -= 1
        }
        fragment = fragment.parentFragment
    }
    return (if (skipCount == 0) action(requireActivity()) else null)
        ?: throw NoSuchElementInHierarchy("Element not found.")
}

public class NoSuchElementInHierarchy(message: String) : IllegalStateException(message)
