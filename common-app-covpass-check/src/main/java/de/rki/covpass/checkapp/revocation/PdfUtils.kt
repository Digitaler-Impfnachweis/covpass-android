/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.checkapp.revocation

import android.content.Context
import com.ensody.reactivestate.DependencyAccessor
import de.rki.covpass.sdk.cert.models.ExpertModeData
import de.rki.covpass.sdk.utils.readTextAsset

@OptIn(DependencyAccessor::class)
internal object PdfUtils {

    internal fun replaceTechnicalDetailsValues(
        context: Context,
        revocationExportData: ExpertModeData,
        base64EncodedCode: String,
        base64EncodedQrCode: String,
    ): String {
        return context.readTextAsset("TechnicalDetailsTemplate.svg")
            .replace(
                "\$code",
                base64EncodedCode
            )
            .replace(
                "\$co",
                revocationExportData.issuingCountry.sanitizeXMLString()
            )
            .replace(
                "\$te",
                revocationExportData.technicalExpiryDate
            )
            .replace(
                "\$qr",
                base64EncodedQrCode
            )
    }
}

private fun String.sanitizeXMLString(): String {
    return this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
