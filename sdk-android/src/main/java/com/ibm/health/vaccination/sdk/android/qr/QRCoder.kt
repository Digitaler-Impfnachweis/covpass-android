package com.ibm.health.vaccination.sdk.android.qr

import com.ibm.health.common.base45.Base45
import com.ibm.health.vaccination.sdk.android.cose.CoseSign1
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.qr.models.ValidationCertificate
import com.ibm.health.vaccination.sdk.android.zlib.Zlib
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray

/**
 * Used to encode/decode QR code string.
 */
public class QRCoder {

    private val cbor: Cbor = Cbor { ignoreUnknownKeys = true }

    internal fun decodeCose(qr: String): CoseSign1 {
        val decodedByteArrayFromBase45 = Base45.decode(qr.toByteArray())
        val decompressedByteArray = Zlib.decompress(decodedByteArrayFromBase45)
        return CoseSign1.fromByteArray(decompressedByteArray)
    }

    private inline fun <reified T> decode(qr: String): T {
        val coseSign1 = decodeCose(qr)
        return cbor.decodeFromByteArray(coseSign1.payload)
    }

    /**
     * Converts a [qrContent] to a [VaccinationCertificate] data model.
     */
    public fun decodeVaccinationCert(qrContent: String): VaccinationCertificate {
        return decode(qrContent)
    }

    /**
     * Converts a [qrContent] to a [ValidationCertificate] data model.
     */
    public fun decodeValidationCert(qrContent: String): ValidationCertificate {
        return decode(qrContent)
    }
}
