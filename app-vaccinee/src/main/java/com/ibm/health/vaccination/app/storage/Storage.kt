package com.ibm.health.vaccination.app.storage

import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate

object Storage {

    private const val PREFS_NAME = "vaccinee_prefs"
    private const val PREFS_KEY_ONBOARDING_SHOWN = "onboarding_shown"
    private const val PREFS_KEY_QR_CONTENT = "qr_content"
    private const val PREFS_KEY_VACCINATION_CERTIFICATE = "vaccination_certificate"

    private val keyValueStore by lazy { EncryptedKeyValueStore(androidDeps.application, PREFS_NAME) }

    var onboardingDone: Boolean
        get() {
            return keyValueStore.get(PREFS_KEY_ONBOARDING_SHOWN) as? Boolean ?: false
        }
        set(value) {
            keyValueStore.set(PREFS_KEY_ONBOARDING_SHOWN, value)
        }

    // FIXME this is just a provisionally implementation
    fun getQrContent(): String? = keyValueStore.get(PREFS_KEY_QR_CONTENT)

    // FIXME this is just a provisionally implementation
    fun setQrContent(qrContent: String) = keyValueStore.set(PREFS_KEY_QR_CONTENT, qrContent)

    // FIXME this is just a provisionally implementation
    fun getVaccinationCertificate(): VaccinationCertificate? = keyValueStore.get(PREFS_KEY_VACCINATION_CERTIFICATE)

    // FIXME this is just a provisionally implementation
    fun setVaccinationCertificate(vaccinationCertificate: VaccinationCertificate) =
        keyValueStore.set(PREFS_KEY_VACCINATION_CERTIFICATE, vaccinationCertificate)
}
