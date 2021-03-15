package com.ibm.health.common.android.utils

/**
 * Base interface for listening the request permission result in a fragment/activity.
 */
public interface OnRequestPermissionsResultHook {
    public fun onRequestPermissionsResultHook(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
    }
}
