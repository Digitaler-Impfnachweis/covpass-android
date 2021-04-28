package com.ibm.health.vaccination.sdk.android.qr

import COSE.CoseException
import COSE.OneKey
import COSE.Sign1Message
import com.ibm.health.common.base45.Base45
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.isCA
import com.ibm.health.vaccination.sdk.android.qr.models.VaccinationCertificate
import com.ibm.health.vaccination.sdk.android.qr.models.ValidationCertificate
import com.ibm.health.vaccination.sdk.android.zlib.Zlib
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.security.GeneralSecurityException
import java.time.LocalDate

/**
 * Used to encode/decode QR code string.
 */
public class QRCoder(private val validator: CertValidator) {

    private val cbor: Cbor = Cbor { ignoreUnknownKeys = true }

    internal fun decodeRawCose(qr: String): ByteArray =
        Zlib.decompress(Base45.decode(qr.toByteArray()))

    internal fun decodeCose(qr: String): Sign1Message =
        Sign1Message.DecodeFromBytes(decodeRawCose(qr)) as? Sign1Message
            ?: throw CoseException("Not a cose-sign1 message")

    private inline fun <reified T> decode(qr: String): T {
        val cose = decodeCose(qr)
        validate(cose)
        return cbor.decodeFromByteArray(cose.GetContent())
    }

    private fun validate(cose: Sign1Message) {
        val validationCert = cbor.decodeFromByteArray<ValidationCertificate>(cose.GetContent())
        // TODO/FIXME: Nullability should not be possible, but currently the certs don't have an expiry attribute
        if (validationCert.validUntil?.isAfter(LocalDate.now()) == true) {
            throw HCertExpiredException()
        }

        // TODO: Once the kid contains the correct value we can resolve the cert directly.
        //  Until then, try out all certificates.
        for (cert in validator.trustedCerts.filter { !it.isCA }) {
            try {
                // Validate the COSE signature
                if (cose.validate(OneKey(cert.publicKey, null))) {
                    // TODO: Clarify if we additionally want to validate the cert chain
                    return
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
    public fun decodeVaccinationCert(qrContent: String): VaccinationCertificate =
        decode(qrContent)

    /**
     * Converts a [qrContent] to a [ValidationCertificate] data model.
     *
     * @throws HCertExpiredException If the certificate has expired.
     * @throws HCertBadSignatureException If the signature validation failed.
     * @throws CoseException For generic COSE errors.
     * @throws GeneralSecurityException For generic cryptography errors.
     */
    public fun decodeValidationCert(qrContent: String): ValidationCertificate =
        decode(qrContent)
}

/** Thrown when the HCert expiry validation failed. */
public open class HCertExpiredException(message: String = "Certificate expired") : CoseException(message)

/** Thrown when the HCert signature validation failed. */
public open class HCertBadSignatureException(message: String = "Validation failed") : CoseException(message)
