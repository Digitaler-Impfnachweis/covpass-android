package com.ibm.health.common.android.utils

/**
 * Base interface for anything that must get initialized e.g. in an onCreate method or the constructor.
 *
 * This is useful when wanting to add arbitrary functionality to a fragment/activity just by interface inheritance.
 */
public interface OnCreateHook {
    public fun onCreateHook() {}
}
