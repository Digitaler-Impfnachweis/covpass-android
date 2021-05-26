/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore
import de.rki.covpass.sdk.cert.models.VaccinationCertificateList

/**
 * Repository which contains the [GroupedCertificatesList]
 */
// FIXME move to SDK?
internal class CertRepository(store: CborSharedPrefsStore) {

    private val certsPref = store.getData("vaccination_certificate_list", VaccinationCertificateList())

    val certs = SuspendMutableValueFlow(GroupedCertificatesList.fromVaccinationCertificateList(certsPref.value)) {
        certsPref.set(value = it.toVaccinationCertificateList(), force = true)
    }
}
