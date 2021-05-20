/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.app.vaccinee.storage

import com.ensody.reactivestate.MutableValueFlow
import com.ensody.reactivestate.SuspendMutableValueFlow
import com.ibm.health.common.vaccination.app.utils.CborSharedPrefsStore
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificateList

/**
 * Repository which contains the [GroupedCertificatesList]
 */
// FIXME move to SDK?
internal class CertRepository(store: CborSharedPrefsStore) {

    private val certsPref = store.getData("vaccination_certificate_list", VaccinationCertificateList())

    // TODO: Remove this with the next ReactiveState upgrade
    private val certFlow = MutableValueFlow(GroupedCertificatesList.fromVaccinationCertificateList(certsPref.value))

    val certs = SuspendMutableValueFlow(certFlow) {
        certsPref.set(value = it.toVaccinationCertificateList(), force = true)
    }
}
