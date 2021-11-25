/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

public interface OnUserInteractionListener {
    /**
     * Hook to get notified whenever a key, touch, or trackball event is dispatched to the current activity.
     */
    public fun onUserInteraction()
}
