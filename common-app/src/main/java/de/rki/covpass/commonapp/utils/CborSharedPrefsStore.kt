/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.commonapp.utils

import android.util.Base64
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.android.utils.SharedPrefsStore
import de.rki.covpass.sdk.android.utils.serialization.InstantSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

/**
 * Wrapper around [SharedPrefsStore], encoding everything with CBOR.
 */
public class CborSharedPrefsStore(public val prefs: SharedPrefsStore) {

    public constructor(preferencesName: String) : this(SharedPrefsStore(getEncryptedSharedPreferences(preferencesName)))

    private val module = SerializersModule {
        contextual(InstantSerializer)
    }

    public val cbor: Cbor = Cbor {
        ignoreUnknownKeys = true
        serializersModule = module
    }

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
}
