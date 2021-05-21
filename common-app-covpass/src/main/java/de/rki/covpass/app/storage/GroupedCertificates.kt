/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.storage

import de.rki.covpass.sdk.android.cert.models.CombinedVaccinationCertificate

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
// TODO maybe move this to sdk later on
internal data class GroupedCertificates(
    var completeCertificate: CombinedVaccinationCertificate?,
    var incompleteCertificate: CombinedVaccinationCertificate?,
) {

    fun getMainCertId() =
        getMainCertificate().vaccinationCertificate.vaccination.id

    /**
     * @return True, if the id of the incomplete or complete cert matches the given [certId], else false.
     */
    fun matchesId(certId: String) =
        completeCertificate?.vaccinationCertificate?.vaccination?.id == certId ||
            incompleteCertificate?.vaccinationCertificate?.vaccination?.id == certId

    fun getMainCertificate() =
        completeCertificate
            ?: incompleteCertificate
            ?: throw IllegalStateException("Either completeCertificate or incompleteCertificate must be set")

    fun isComplete() = completeCertificate != null

    fun isSingleVaccination() = completeCertificate == null || incompleteCertificate == null

    fun removeCert(certId: String) {
        if (incompleteCertificate?.vaccinationCertificate?.vaccination?.id == certId) {
            incompleteCertificate = null
        }
        if (completeCertificate?.vaccinationCertificate?.vaccination?.id == certId) {
            completeCertificate = null
        }
    }
}
