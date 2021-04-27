package com.ibm.health.vaccination.app.vaccinee.storage

import com.ensody.reactivestate.MutableValueFlow
import com.ibm.health.common.android.utils.SettableStateFlow
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificateList

class Storage {

    private val keyValueStore = EncryptedKeyValueStore(androidDeps.application, "vaccinee_prefs")

    private val certsPref = keyValueStore.getFlow("vaccination_certificate_list", VaccinationCertificateList())

    private val certFlow = MutableValueFlow(GroupedCertificatesList.fromVaccinationCertificateList(certsPref.value))

    val onboardingDone = keyValueStore.getFlow("onboarding_shown", false)

    // FIXME move to SDK as CertificateStorage
    // FIXME do this directly when updating the MutableValueFlow, not from outside
    val certs = SettableStateFlow(certFlow) {
        certsPref.set(it.toVaccinationCertificateList())
        certFlow.emit(it)
    }
}
