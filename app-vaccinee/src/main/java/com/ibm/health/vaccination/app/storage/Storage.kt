package com.ibm.health.vaccination.app.storage

import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.dispatchers
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificateList
import kotlinx.coroutines.invoke

object Storage {

    private const val PREFS_NAME = "vaccinee_prefs"
    private const val PREFS_KEY_ONBOARDING_SHOWN = "onboarding_shown"
    private const val PREFS_KEY_VACCINATION_CERTIFICATE_LIST = "vaccination_certificate_list"

    private val keyValueStore by lazy { EncryptedKeyValueStore(androidDeps.application, PREFS_NAME) }

    val certCache = MutableValueFlow(
        keyValueStore.get(PREFS_KEY_VACCINATION_CERTIFICATE_LIST)
            ?: VaccinationCertificateList()
    )

    var onboardingDone: Boolean
        get() {
            return keyValueStore.get(PREFS_KEY_ONBOARDING_SHOWN) as? Boolean ?: false
        }
        set(value) {
            keyValueStore.set(PREFS_KEY_ONBOARDING_SHOWN, value)
        }

    // FIXME move to SDK as CertificateStorage
    suspend fun setVaccinationCertificateList(certificateList: VaccinationCertificateList) {
        dispatchers.io {
            keyValueStore.set(PREFS_KEY_VACCINATION_CERTIFICATE_LIST, certificateList)
            certCache.value = certificateList
        }
    }
}
