/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import android.content.SharedPreferences
import android.util.Base64
import com.ensody.reactivestate.SuspendMutableValueFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

/**
 * Wrapper around [SharedPrefsStore], encoding everything with CBOR.
 */
public class CborSharedPrefsStore(
    public val prefs: SharedPrefsStore,
    public val cbor: Cbor,
) {

    public constructor(
        preferencesName: String,
        cbor: Cbor,
    ) : this(SharedPrefsStore(getEncryptedSharedPreferences(preferencesName)), cbor)

    /**
     * @param key Used for access to the [T] object
     * @return an object from the storage by provided [key], null otherwise.
     */
    public inline fun <reified T : Any> getData(key: String, default: T): SuspendMutableValueFlow<T> {
        val flow = prefs.getData(key, "")
        val value = try {
            if (flow.value.isEmpty()) default
            else cbor.decodeFromByteArray(Base64.decode(flow.value, Base64.DEFAULT))
        } catch (e: SerializationException) {
            default
        }
        return SuspendMutableValueFlow(value) {
            flow.set(Base64.encodeToString(cbor.encodeToByteArray(it), Base64.DEFAULT))
        }
    }

    /**
     * @see SharedPreferences.contains
     */
    public operator fun contains(key: String): Boolean = key in prefs
}
