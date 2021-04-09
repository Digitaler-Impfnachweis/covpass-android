package com.ibm.health.vaccination.sdk.android.qr

import com.ibm.health.common.base45.Base45
import com.ibm.health.vaccination.sdk.android.cose.CoseSign1
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.zlib.Zlib
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray

/**
 * Used to decode QR code string.
 */
@ExperimentalSerializationApi
public class QRDecoder {

    private val cbor: Cbor = Cbor { ignoreUnknownKeys = true }

    /**
     * Decodes [qr] code.
     * @return [VaccinationCertificate] data model.
     */
    public fun decode(qr: String): VaccinationCertificate {
        val decodedByteArrayFromBase45 = Base45.decode(qr.toByteArray())
        val decompressedByteArray = Zlib.decompress(decodedByteArrayFromBase45)
        val coseSign1 = CoseSign1.fromByteArray(decompressedByteArray)
        return cbor.decodeFromByteArray(coseSign1.payload)
    }
}
