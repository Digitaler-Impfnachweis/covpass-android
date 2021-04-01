package com.ibm.health.vaccination.app.storage

import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore

object Storage {

    private const val PREFS_NAME = "cert_checker_prefs"
    private const val PREFS_KEY_ONBOARDING_SHOWN = "onboarding_shown"

    private val keyValueStore by lazy { EncryptedKeyValueStore(androidDeps.application, PREFS_NAME) }

    var onboardingDone: Boolean
        get() {
            return keyValueStore.get(PREFS_KEY_ONBOARDING_SHOWN) as? Boolean ?: false
        }
        set(value) {
            keyValueStore.set(PREFS_KEY_ONBOARDING_SHOWN, value)
        }
}
