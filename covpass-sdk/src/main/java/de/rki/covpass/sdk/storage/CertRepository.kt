/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.storage

import com.ensody.reactivestate.SuspendMutableValueFlow
import de.rki.covpass.sdk.cert.models.CERT_DATA_MODEL_VERSION
import de.rki.covpass.sdk.cert.models.CovCertificateList
import de.rki.covpass.sdk.cert.models.CovCertificateListVersion
import de.rki.covpass.sdk.cert.models.GroupedCertificatesList
import de.rki.covpass.sdk.storage.migration.MigrationFromVersion1To2
import kotlinx.coroutines.runBlocking

/**
 * Repository which contains the [GroupedCertificatesList]
 */
public class CertRepository(private val store: CborSharedPrefsStore) {

    public val certs: SuspendMutableValueFlow<GroupedCertificatesList>

    private val certsPref: SuspendMutableValueFlow<CovCertificateList>

    // this one is only used for checking if a data migration is necessary
    private val certsPrefVersion: SuspendMutableValueFlow<CovCertificateListVersion>

    init {
        runBlocking {
            // Before using the data, check if the user has an outdated data model version. This can happen after
            // updating the app. If so, do a data migration.
            assertCorrectDataModelVersion()
        }
        // After the migration is done, we can initialize the fields.
        certsPref = store.getData(PREFS_KEY_CERT_LIST, CovCertificateList())
        certsPrefVersion = store.getData(PREFS_KEY_CERT_LIST, CovCertificateListVersion())
        certs = SuspendMutableValueFlow(GroupedCertificatesList.fromCovCertificateList(certsPref.value)) {
            certsPref.set(value = it.toCovCertificateList(), force = true)
        }
    }

    private suspend fun assertCorrectDataModelVersion() {
        if (PREFS_KEY_CERT_LIST !in store) {
            // no data, no migration
            return
        }
        var versionFromStorage = store.getData(PREFS_KEY_CERT_LIST, CovCertificateListVersion()).value.dataModelVersion
        while (versionFromStorage < CERT_DATA_MODEL_VERSION) {
            migrateData(versionFromStorage)
            versionFromStorage = store.getData(PREFS_KEY_CERT_LIST, CovCertificateListVersion()).value.dataModelVersion
        }
    }

    private suspend fun migrateData(versionFromStorage: Int) {
        when (versionFromStorage) {
            1 -> MigrationFromVersion1To2(store).migrate()
        }
    }

    internal companion object {
        internal const val PREFS_KEY_CERT_LIST = "vaccination_certificate_list"
    }
}
