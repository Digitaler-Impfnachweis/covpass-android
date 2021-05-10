package com.ibm.health.common.vaccination.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ibm.health.common.android.utils.androidDeps

public fun getEncryptedSharedPreferences(preferencesName: String): SharedPreferences =
    getEncryptedSharedPreferences(context = androidDeps.application, preferencesName = preferencesName)

public fun getEncryptedSharedPreferences(context: Context, preferencesName: String): SharedPreferences {
    val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        preferencesName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
