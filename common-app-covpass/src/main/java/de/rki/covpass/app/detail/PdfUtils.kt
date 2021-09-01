/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.content.Context
import de.rki.covpass.sdk.cert.*
import de.rki.covpass.sdk.cert.models.CombinedCovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.cert.models.Vaccination
import de.rki.covpass.sdk.utils.formatDateInternational
import de.rki.covpass.sdk.utils.readTextAsset

internal object PdfUtils {

    fun replaceVaccinationValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        vaccination: Vaccination
    ): String = context.readTextAsset("VaccinationCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullName.sanitizeXMLString())
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted.sanitizeXMLString())
        .replace("\$ci", vaccination.id.sanitizeXMLString())
        .replace("\$tg", getDiseaseAgentName(vaccination.targetDisease).sanitizeXMLString())
        .replace("\$vp", getProphylaxisName(vaccination.vaccineCode).sanitizeXMLString())
        .replace("\$mp", getProductName(vaccination.product).sanitizeXMLString())
        .replace("\$ma", getManufacturerName(vaccination.manufacturer).sanitizeXMLString())
        .replace("\$dn", vaccination.doseNumber.toString())
        .replace("\$sd", vaccination.totalSerialDoses.toString())
        .replace("\$dt", vaccination.occurrence?.formatDateInternational() ?: "")
        .replace("\$co", getCountryName(vaccination.country).sanitizeXMLString())
        .replace("\$is", vaccination.certificateIssuer.sanitizeXMLString())
        .replace("\$qr", base64EncodedQrCode)

    fun replaceRecoveryValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        recovery: Recovery
    ): String = context.readTextAsset("RecoveryCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullName.sanitizeXMLString())
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted.sanitizeXMLString())
        .replace("\$ci", recovery.id.sanitizeXMLString())
        .replace("\$tg", getDiseaseAgentName(recovery.targetDisease).sanitizeXMLString())
        .replace("\$fr", recovery.firstResult?.formatDateInternational() ?: "")
        .replace("\$co", getCountryName(recovery.country).sanitizeXMLString())
        .replace("\$is", recovery.certificateIssuer.sanitizeXMLString())
        .replace("\$df", recovery.validFrom?.formatDateInternational() ?: "")
        .replace("\$du", recovery.validUntil?.formatDateInternational() ?: "")
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

public fun String.sanitizeFileName(): String {
    val regex = Regex("[*\\\\/.]")
    return regex.replace(this, "")
}
