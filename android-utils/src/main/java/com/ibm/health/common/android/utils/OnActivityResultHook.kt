package com.ibm.health.common.android.utils

import android.content.Intent

/**
 * Base interface for listening to the result in a fragment/activity.
 */
public interface OnActivityResultHook {
    public fun onActivityResultHook(requestCode: Int, resultCode: Int, data: Intent?) {}
}
