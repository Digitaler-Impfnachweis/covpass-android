/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.util.Base64
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.android.utils.SharedPrefsStore
import de.rki.covpass.sdk.dependencies.sdkDeps
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

/**
 * Wrapper around [SharedPrefsStore], encoding everything with CBOR.
 */
public class CborSharedPrefsStore(public val prefs: SharedPrefsStore) {

    public constructor(preferencesName: String) : this(SharedPrefsStore(getEncryptedSharedPreferences(preferencesName)))

    /**
     * @param key Used for access to the [T] object
     * @return an object from the storage by provided [key], null otherwise.
     */
    public inline fun <reified T : Any> getData(key: String, default: T): SuspendMutableValueFlow<T> {
        val flow = prefs.getData(key, "")
        val value = try {
            if (flow.value.isEmpty()) default
            else sdkDeps.cbor.decodeFromByteArray(Base64.decode(flow.value, Base64.DEFAULT))
        } catch (e: SerializationException) {
            default
        }
        return SuspendMutableValueFlow(value) {
            flow.set(Base64.encodeToString(sdkDeps.cbor.encodeToByteArray(it), Base64.DEFAULT))
        }
    }

    public operator fun contains(key: String): Boolean = key in prefs
}
