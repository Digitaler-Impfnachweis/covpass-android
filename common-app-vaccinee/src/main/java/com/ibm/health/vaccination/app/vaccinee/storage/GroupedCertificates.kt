package com.ibm.health.vaccination.app.vaccinee.storage

import com.ibm.health.vaccination.sdk.android.cert.models.CombinedVaccinationCertificate

/**
 * Data model which groups together a complete and an incomplete certificate (if available).
 */
// TODO maybe move this to sdk later on
internal data class GroupedCertificates(
    val completeCertificate: CombinedVaccinationCertificate?,
    val incompleteCertificate: CombinedVaccinationCertificate?,
) {

    fun getMainCertId() =
        getMainCertificate().vaccinationCertificate.vaccination.id

    // XXX: Usually always the [getMainCertId] should be considered, but right after scanning, the fragments can
    // still point to the "not main cert id". So it is necessary to check both ids.
    fun matchesId(certId: String) =
        completeCertificate?.vaccinationCertificate?.vaccination?.id == certId ||
            incompleteCertificate?.vaccinationCertificate?.vaccination?.id == certId

    fun getMainCertificate() =
        completeCertificate
            ?: incompleteCertificate
            ?: throw IllegalStateException("Either completeCertificate or incompleteCertificate must be set")

    fun isComplete() = completeCertificate != null
}
