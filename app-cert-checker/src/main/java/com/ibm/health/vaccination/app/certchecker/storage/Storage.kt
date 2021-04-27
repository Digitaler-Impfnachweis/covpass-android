package com.ibm.health.vaccination.app.certchecker.storage

import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore

class Storage {

    private val keyValueStore by lazy { EncryptedKeyValueStore(androidDeps.application, "cert_checker_prefs") }

    val onboardingDone = keyValueStore.getFlow("onboarding_shown", false)
}
