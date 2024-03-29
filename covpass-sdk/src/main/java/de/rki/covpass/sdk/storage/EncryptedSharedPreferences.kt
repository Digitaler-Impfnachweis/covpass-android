/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.sdk.dependencies.sdkDeps

/**
 * @return Encrypted [SharedPreferences] for the given [preferencesName] using application context.
 */
@OptIn(DependencyAccessor::class)
public fun getEncryptedSharedPreferences(preferencesName: String): SharedPreferences =
    getEncryptedSharedPreferences(context = sdkDeps.application, preferencesName = preferencesName)

/**
 * @return Encrypted [SharedPreferences] for the given [preferencesName] and [context].
 */
public fun getEncryptedSharedPreferences(context: Context, preferencesName: String): SharedPreferences {
    val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        preferencesName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
