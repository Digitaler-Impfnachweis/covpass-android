/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.android.utils

import android.content.SharedPreferences
import androidx.core.content.edit
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ensody.reactivestate.dispatchers
import kotlinx.coroutines.invoke

/**
 * Wrapper around [SharedPreferences], providing access to values via [SuspendMutableValueFlow].
 */
public class SharedPrefsStore(public val prefs: SharedPreferences) {

    public fun getData(key: String, default: String): SuspendMutableValueFlow<String> =
        getFlow(prefs.getString(key, default) ?: default) {
            putString(key, it)
        }

    public fun getData(key: String, default: Int): SuspendMutableValueFlow<Int> =
        getFlow(prefs.getInt(key, default)) {
            putInt(key, it)
        }

    public fun getData(key: String, default: Long): SuspendMutableValueFlow<Long> =
        getFlow(prefs.getLong(key, default)) {
            putLong(key, it)
        }

    public fun getData(key: String, default: Float): SuspendMutableValueFlow<Float> =
        getFlow(prefs.getFloat(key, default)) {
            putFloat(key, it)
        }

    public fun getData(key: String, default: Boolean): SuspendMutableValueFlow<Boolean> =
        getFlow(prefs.getBoolean(key, default)) {
            putBoolean(key, it)
        }

    private fun <T> getFlow(default: T, setter: SharedPreferences.Editor.(value: T) -> Unit) =
        SuspendMutableValueFlow(default) {
            dispatchers.io {
                prefs.edit { setter(it) }
            }
        }
}
