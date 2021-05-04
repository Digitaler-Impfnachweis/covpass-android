package com.ibm.health.vaccination.app.vaccinee.storage

import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.android.utils.androidDeps
import com.ibm.health.common.vaccination.app.EncryptedKeyValueStore
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificateList

// TODO: Should this be named CertRepository or CertStorage?
class Storage {

    private val store = EncryptedKeyValueStore(androidDeps.application, "vaccinee_prefs")

    private val certsPref = store.getFlow("vaccination_certificate_list", VaccinationCertificateList())

    private val certFlow = MutableValueFlow(GroupedCertificatesList.fromVaccinationCertificateList(certsPref.value))

    // TODO: Split this up into a separate storage system? This could even be reused via common-app.
    val onboardingDone = store.getFlow("onboarding_shown", false)

    // FIXME move to SDK as CertificateStorage
    val certs = SuspendMutableValueFlow(certFlow) {
        certsPref.set(it.toVaccinationCertificateList())
    }
}
