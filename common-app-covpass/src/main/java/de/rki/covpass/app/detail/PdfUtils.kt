/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.content.Context
import de.rki.covpass.app.validitycheck.countries.CountryRepository
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.TestCert
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.formatDateTimeInternational
import de.rki.covpass.sdk.utils.readTextAsset

internal object PdfUtils {

    fun replaceVaccinationValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        vaccination: Vaccination,
    ): String = context.readTextAsset("VaccinationCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullNameReverse.sanitizeXMLString())
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted.sanitizeXMLString())
        .replace("\$ci", vaccination.idWithoutPrefix.sanitizeXMLString())
        .replace("\$tg", getDiseaseAgentName(vaccination.targetDisease).sanitizeXMLString())
        .replace("\$vp", getProphylaxisName(vaccination.vaccineCode).sanitizeXMLString())
        .replace("\$mp", getProductName(vaccination.product).sanitizeXMLString())
        .replace("\$ma", getManufacturerName(vaccination.manufacturer).sanitizeXMLString())
        .replace("\$dn", vaccination.doseNumber.toString())
        .replace("\$sd", vaccination.totalSerialDoses.toString())
        .replace("\$dt", vaccination.occurrence?.formatDateInternational() ?: "")
        .replace("\$co", CountryRepository.getCountryLocalized(vaccination.country).sanitizeXMLString())
        .replace("\$is", vaccination.certificateIssuer.sanitizeXMLString())
        .replace("\$qr", base64EncodedQrCode)

    fun replaceRecoveryValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        recovery: Recovery,
    ): String = context.readTextAsset("RecoveryCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullNameReverse.sanitizeXMLString())
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted.sanitizeXMLString())
        .replace("\$ci", recovery.idWithoutPrefix.sanitizeXMLString())
        .replace("\$tg", getDiseaseAgentName(recovery.targetDisease).sanitizeXMLString())
        .replace("\$fr", recovery.firstResult?.formatDateInternational() ?: "")
        .replace("\$co", CountryRepository.getCountryLocalized(recovery.country).sanitizeXMLString())
        .replace("\$is", recovery.certificateIssuer.sanitizeXMLString())
        .replace("\$df", recovery.validFrom?.formatDateInternational() ?: "")
        .replace("\$du", recovery.validUntil?.formatDateInternational() ?: "")
        .replace("\$qr", base64EncodedQrCode)

    fun replaceTestCertificateValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        testCert: TestCert,
    ): String = context.readTextAsset("TestCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullNameReverse.sanitizeXMLString())
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted.sanitizeXMLString())
        .replace("\$ci", testCert.idWithoutPrefix.sanitizeXMLString())
        .replace("\$tg", getDiseaseAgentName(testCert.targetDisease).sanitizeXMLString())
        .replace("\$tt", getTestTypeName(testCert.testType).sanitizeXMLString())
        .replace("\$nm", testCert.testName ?: "")
        .replace("\$ma", getTestManufacturerName(testCert.manufacturer ?: "").sanitizeXMLString())
        .replace("\$sc", testCert.sampleCollection?.formatDateTimeInternational() ?: "")
        .replace("\$tr", getTestResultName(testCert.testResult).sanitizeXMLString())
        .replace("\$tc", testCert.testingCenter)
        .replace("\$co", CountryRepository.getCountryLocalized(testCert.country).sanitizeXMLString())
        .replace("\$is", testCert.certificateIssuer.sanitizeXMLString())
        .replace("\$qr", base64EncodedQrCode)
}

private fun String.sanitizeXMLString(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
