/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ensody.reactivestate.dispatchers
import kotlinx.coroutines.invoke

/**
 * Wrapper around [SharedPreferences], providing access to values via [SuspendMutableValueFlow].
 */
public class SharedPrefsStore(public val prefs: SharedPreferences) {

    /**
     * @return A [SuspendMutableValueFlow] with a [String] value as data
     * from the [SharedPreferences] for the given [key].
     */
    public fun getData(key: String, default: String): SuspendMutableValueFlow<String> =
        getFlow(prefs.getString(key, default) ?: default) {
            putString(key, it)
        }

    /**
     * @return A [SuspendMutableValueFlow] with a [Int] value as data
     * from the [SharedPreferences] for the given [key].
     */
    public fun getData(key: String, default: Int): SuspendMutableValueFlow<Int> =
        getFlow(prefs.getInt(key, default)) {
            putInt(key, it)
        }

    /**
     * @return A [SuspendMutableValueFlow] with a [Long] value as data
     * from the [SharedPreferences] for the given [key].
     */
    public fun getData(key: String, default: Long): SuspendMutableValueFlow<Long> =
        getFlow(prefs.getLong(key, default)) {
            putLong(key, it)
        }

    /**
     * @return A [SuspendMutableValueFlow] with a [Float] value as data
     * from the [SharedPreferences] for the given [key].
     */
    public fun getData(key: String, default: Float): SuspendMutableValueFlow<Float> =
        getFlow(prefs.getFloat(key, default)) {
            putFloat(key, it)
        }

    /**
     * @return A [SuspendMutableValueFlow] with a [Boolean] value as data
     * from the [SharedPreferences] for the given [key].
     */
    public fun getData(key: String, default: Boolean): SuspendMutableValueFlow<Boolean> =
        getFlow(prefs.getBoolean(key, default)) {
            putBoolean(key, it)
        }

    /**
     * @see SharedPreferences.contains
     */
    public operator fun contains(key: String): Boolean = key in prefs

    private fun <T> getFlow(default: T, setter: SharedPreferences.Editor.(value: T) -> Unit) =
        SuspendMutableValueFlow(default) {
            dispatchers.io {
                prefs.edit { setter(it) }
            }
        }
}
