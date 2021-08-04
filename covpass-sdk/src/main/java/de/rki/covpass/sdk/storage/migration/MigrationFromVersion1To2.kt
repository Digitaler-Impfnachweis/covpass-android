package de.rki.covpass.sdk.storage.migration

import de.rki.covpass.sdk.cert.models.CovCertificateList
import de.rki.covpass.sdk.cert.models.GroupedCertificatesId
import de.rki.covpass.sdk.cert.models.migration.Version1CertList
import de.rki.covpass.sdk.storage.CborSharedPrefsStore
import de.rki.covpass.sdk.storage.CertRepository.Companion.PREFS_KEY_CERT_LIST

/**
 * Logic for migrating from cert data model version 1 to 2.
 */
internal class MigrationFromVersion1To2(store: CborSharedPrefsStore) {

    private val version1CertsPref = store.getData(PREFS_KEY_CERT_LIST, Version1CertList())
    private val version2CertsPref = store.getData(PREFS_KEY_CERT_LIST, CovCertificateList())

    /**
     * In version 1 the favorite id was stored as string, now as [GroupedCertificatesId]. This will be migrated here.
     */
    internal suspend fun migrate() {
        val version1CertList = version1CertsPref.value
        var newFavoriteId: GroupedCertificatesId? = null
        version1CertList.certificates.forEach { cert ->
            val id = cert.covCertificate.vaccination?.id
            val isFavorite = !id.isNullOrBlank() && id == version1CertList.favoriteCertId
            if (isFavorite) {
                newFavoriteId =
                    GroupedCertificatesId(cert.covCertificate.name.trimmedName, cert.covCertificate.birthDate)
                return@forEach
            }
        }
        val newData = CovCertificateList(version1CertList.certificates, newFavoriteId)
        version2CertsPref.set(newData)
    }
}
