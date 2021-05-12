package com.ibm.health.vaccination.sdk.android.cert

import COSE.CoseException
import COSE.OneKey
import COSE.Sign1Message
import com.ibm.health.common.base45.Base45
import com.ibm.health.vaccination.sdk.android.cert.models.CBORWebToken
import com.ibm.health.vaccination.sdk.android.cert.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.isCA
import com.ibm.health.vaccination.sdk.android.utils.Zlib
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.security.GeneralSecurityException
import java.time.Instant

/**
 * Used to encode/decode QR code string.
 */
public class QRCoder(private val validator: CertValidator) {

    private val cbor: Cbor = Cbor { ignoreUnknownKeys = true }

    /** Returns the raw COSE ByteArray contained within the certificate. */
    internal fun decodeRawCose(qr: String): ByteArray {
        var qrContent: String = qr
        if (qrContent.startsWith("HC1:")) {
            qrContent = qrContent.removePrefix("HC1:")
        }
        return Zlib.decompress(Base45.decode(qrContent.toByteArray()))
    }

    internal fun decodeCose(qr: String): Sign1Message =
        Sign1Message.DecodeFromBytes(decodeRawCose(qr)) as? Sign1Message
            ?: throw CoseException("Not a cose-sign1 message")

    private fun decodeCWT(qr: String): CBORWebToken =
        validate(decodeCose(qr))

    private fun validate(cose: Sign1Message): CBORWebToken {
        val cwt = CBORWebToken.decode(cose.GetContent())

        if (cwt.validUntil.isBefore(Instant.now())) {
            throw HCertExpiredException()
        }

        // TODO: Once the kid contains the correct value we can resolve the cert directly.
        //  Until then, try out all certificates.
        for (cert in validator.trustedCerts.filter { !it.isCA }) {
            try {
                // Validate the COSE signature
                if (cose.validate(OneKey(cert.publicKey, null))) {
                    return cwt
                }
            } catch (e: CoseException) {
                continue
            } catch (e: GeneralSecurityException) {
                continue
            }
        }
        throw HCertBadSignatureException()
    }

    /**
     * Converts a [qrContent] to a [VaccinationCertificate] data model.
     *
     * @throws HCertExpiredException If the certificate has expired.
     * @throws HCertBadSignatureException If the signature validation failed.
     * @throws CoseException For generic COSE errors.
     * @throws GeneralSecurityException For generic cryptography errors.
     */
    public fun decodeVaccinationCert(qrContent: String): VaccinationCertificate {
        val cwt = decodeCWT(qrContent)
        val cert: VaccinationCertificate =
            cbor.decodeFromByteArray(cwt.rawCbor[HEALTH_CERTIFICATE_CLAIM][DIGITAL_GREEN_CERTIFICATE].EncodeToBytes())
        return cert.copy(issuer = cwt.issuer, validFrom = cwt.validFrom, validUntil = cwt.validUntil)
    }

    private companion object {
        private const val HEALTH_CERTIFICATE_CLAIM = -260
        private const val DIGITAL_GREEN_CERTIFICATE = 1
    }
}

/** Thrown when the HCert expiry validation failed. */
public open class HCertExpiredException(message: String = "Certificate expired") : CoseException(message)

/** Thrown when the HCert signature validation failed. */
public open class HCertBadSignatureException(message: String = "Validation failed") : CoseException(message)
