package com.ibm.health.vaccination.sdk.android.cert.models

import com.ibm.health.vaccination.sdk.android.cert.QRCoder
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Service for getting validation qr codes from the backend for given vaccination certs.
 */
public class CertService(httpClient: HttpClient, host: String, private val qrCoder: QRCoder) {
    private val client = httpClient.config {
        defaultRequest {
            this.host = host
        }
    }

    /** Gets the validation cert QR code for the given vaccination QR code. */
    public suspend fun getValidationCert(vaccinationQrContent: String): String {
        return qrCoder.encodeRawCose(getValidationCertCose(qrCoder.decodeRawCose(vaccinationQrContent)))
    }

    /** Gets the raw COSE validation cert for the given raw vaccination COSE object. */
    public suspend fun getValidationCertCose(rawVaccinationCose: ByteArray): ByteArray {
        val response: HttpResponse = client.post("/api/certify/v1/reissue/cbor") {
            contentType(ContentType.parse("application/cbor"))
            body = rawVaccinationCose
        }
        return response.readBytes()
    }
}
