/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import de.rki.covpass.commonapp.pdfexport.BaseExportPdfViewModel
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.sanitizeFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel to handle business logic related to [DetailExportPdfFragment].
 */
internal class DetailExportPdfViewModel constructor(
    scope: CoroutineScope,
) : BaseExportPdfViewModel(scope) {

    private val fileName: MutableStateFlow<String> = MutableStateFlow("")

    override fun getFileName(): String = "Certificate-${fileName.value}.pdf".sanitizeFileName()

    fun onShareClick(combinedCovCertificate: CombinedCovCertificate) {
        launch {
            fileName.value = combinedCovCertificate.covCertificate.fullName.replace(" ", "-")
            pdfString.value = when (val dgcEntry = combinedCovCertificate.covCertificate.dgcEntry) {
                is Vaccination -> {
                    PdfUtils.replaceVaccinationValues(
                        applicationContext,
                        combinedCovCertificate,
                        combinedCovCertificate.qrContent.toBase64EncodedString(),
                        dgcEntry
                    )
                }
                is Recovery -> {
                    PdfUtils.replaceRecoveryValues(
                        applicationContext,
                        combinedCovCertificate,
                        combinedCovCertificate.qrContent.toBase64EncodedString(),
                        dgcEntry
                    )
                }
                is TestCert -> {
                    PdfUtils.replaceTestCertificateValues(
                        applicationContext,
                        combinedCovCertificate,
                        combinedCovCertificate.qrContent.toBase64EncodedString(),
                        dgcEntry
                    )
                }
            }
        }
    }
}
