/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.app.detail

import android.content.Context
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
        .replace("\$nam", combinedCertificate.covCertificate.fullName)
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted)
        .replace("\$ci", vaccination.id)
        .replace("\$tg", vaccination.targetDisease)
        .replace("\$vp", vaccination.vaccineCode)
        .replace("\$mp", vaccination.product)
        .replace("\$ma", vaccination.manufacturer)
        .replace("\$dn", vaccination.doseNumber.toString())
        .replace("\$sd", vaccination.totalSerialDoses.toString())
        .replace("\$dt", vaccination.occurrence?.formatDateInternational() ?: "")
        .replace("\$co", vaccination.country)
        .replace("\$is", vaccination.certificateIssuer)
        .replace("\$qr", base64EncodedQrCode)

    fun replaceRecoveryValues(
        context: Context,
        combinedCertificate: CombinedCovCertificate,
        base64EncodedQrCode: String,
        recovery: Recovery
    ): String = context.readTextAsset("RecoveryCertificateTemplate.svg")
        .replace("\$nam", combinedCertificate.covCertificate.fullName)
        .replace("\$dob", combinedCertificate.covCertificate.birthDateFormatted)
        .replace("\$ci", recovery.id)
        .replace("\$tg", recovery.targetDisease)
        .replace("\$fr", recovery.firstResult?.formatDateInternational() ?: "")
        .replace("\$co", recovery.country)
        .replace("\$is", recovery.certificateIssuer)
        .replace("\$df", recovery.validFrom?.formatDateInternational() ?: "")
        .replace("\$du", recovery.validUntil?.formatDateInternational() ?: "")
        .replace("\$qr", base64EncodedQrCode)
}
