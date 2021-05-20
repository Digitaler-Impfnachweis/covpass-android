/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.navigation.android

import androidx.fragment.app.Fragment

/**
 * Searches from the current fragment upwards to parents and then the activity until [action] returns a non-null result.
 *
 * @return The non-null return value of [action].
 * @throws [NoSuchElementInHierarchy] if nothing was found.
 *
 * @param skip How many results to skip. Defaults to 0 (taking first result).
 */
public fun <T : Any> Fragment.findInHierarchy(skip: Int = 0, action: (Any) -> T?): T =
    findInHierarchyOrNull(skip = skip, action = action)
        ?: throw NoSuchElementInHierarchy("Element not found.")

/**
 * Searches from the current fragment upwards to parents and then the activity until [action] returns a non-null result.
 *
 * @return The return value of [action] or null if nothing could be found.
 *
 * @param skip How many results to skip. Defaults to 0 (taking first result).
 */
public fun <T : Any> Fragment.findInHierarchyOrNull(skip: Int = 0, action: (Any) -> T?): T? {
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
    return if (skipCount == 0) action(requireActivity()) else null
}

/**
 * Searches from the current fragment upwards to parents and then the activity until the given type is found.
 *
 * @return The matching type.
 * @throws [NoSuchElementInHierarchy] if nothing was found.
 *
 * @param skip How many results to skip. Defaults to 0 (taking first result).
 */
public inline fun <reified T : Any> Fragment.findInHierarchy(skip: Int = 0): T =
    findInHierarchy(skip = skip) { it as? T }

/**
 * Searches from the current fragment upwards to parents and then the activity until the given type is found.
 *
 * @return The matching type or null if nothing could be found.
 *
 * @param skip How many results to skip. Defaults to 0 (taking first result).
 */
public inline fun <reified T : Any> Fragment.findInHierarchyOrNull(skip: Int = 0): T? =
    findInHierarchyOrNull(skip = skip) { it as? T }

/**
 * Thrown when [findInHiercharchy] couldn't find a fragment.
 */
public class NoSuchElementInHierarchy(message: String) : IllegalStateException(message)
