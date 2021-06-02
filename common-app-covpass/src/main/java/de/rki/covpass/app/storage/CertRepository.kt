/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.commonapp.utils.CborSharedPrefsStore
import de.rki.covpass.sdk.cert.models.CovCertificateList

/**
 * Repository which contains the [GroupedCertificatesList]
 */
// FIXME move to SDK?
internal class CertRepository(store: CborSharedPrefsStore) {

    private val certsPref = store.getData("vaccination_certificate_list", CovCertificateList())

    val certs = SuspendMutableValueFlow(GroupedCertificatesList.fromCovCertificateList(certsPref.value)) {
        certsPref.set(value = it.toCovCertificateList(), force = true)
    }
}
