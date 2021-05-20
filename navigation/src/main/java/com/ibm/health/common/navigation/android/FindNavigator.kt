/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import androidx.fragment.app.Fragment

/**
 * Finds the current [Navigator].
 *
 * This can either be a fragment or the hosting activity. This will throw an exception if no [Navigator] was found.
 *
 * @param skip How many [Navigator]s to skip. Defaults to 0.
 */
public fun Fragment.findNavigator(skip: Int = 0): Navigator =
    findInHierarchy(skip = skip) {
        (it as? NavigatorOwner)?.navigator
    }

/**
 * Interface for marking an activity or fragment as the owner of a [Navigator].
 */
public interface NavigatorOwner {
    public val navigator: Navigator
}
