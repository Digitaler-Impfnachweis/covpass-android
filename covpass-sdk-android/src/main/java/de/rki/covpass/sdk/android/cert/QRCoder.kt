/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.cert

import COSE.CoseException
import COSE.Sign1Message
import androidx.annotation.VisibleForTesting
import de.rki.covpass.base45.Base45
import de.rki.covpass.sdk.android.cert.models.CBORWebToken
import de.rki.covpass.sdk.android.cert.models.VaccinationCertificate
import de.rki.covpass.sdk.android.utils.Zlib
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.lang.IndexOutOfBoundsException
import java.security.GeneralSecurityException

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
        validator.validate(decodeCose(qr))

    /**
     * Converts a [qrContent] to a [VaccinationCertificate] data model.
     *
     * @throws ExpiredCwtException If the [CBORWebToken] has expired.
     * @throws BadCoseSignatureException If the signature validation failed.
     * @throws UnsupportedDgcVersionException If the Digital Green Certificate version is unsupported.
     * @throws CoseException For generic COSE errors.
     * @throws GeneralSecurityException For generic cryptography errors.
     */
    public fun decodeVaccinationCert(qrContent: String): VaccinationCertificate {
        val cwt = decodeCWT(qrContent)
        val cert: VaccinationCertificate =
            cbor.decodeFromByteArray(cwt.rawCbor[HEALTH_CERTIFICATE_CLAIM][DIGITAL_GREEN_CERTIFICATE].EncodeToBytes())
        if (!isVersionSupported(cert)) {
            throw UnsupportedDgcVersionException()
        }
        return cert.copy(issuer = cwt.issuer, validFrom = cwt.validFrom, validUntil = cwt.validUntil)
    }

    @VisibleForTesting
    internal fun isVersionSupported(cert: VaccinationCertificate): Boolean {
        val versionSplitted = cert.version.split(".")
        val major = versionSplitted[0].toInt()
        var minor: Int
        try {
            minor = versionSplitted[1].toInt()
        } catch (exception: IndexOutOfBoundsException) {
            // If the minor version is not set, interpret this as 0
            minor = 0
        }
        return major <= VaccinationCertificate.supportedMajorVersion &&
            minor <= VaccinationCertificate.supportedMinorVersion
    }

    private companion object {
        private const val HEALTH_CERTIFICATE_CLAIM = -260
        private const val DIGITAL_GREEN_CERTIFICATE = 1
    }
}

/** Thrown when the Digital Green Certificate has the wrong version. */
public open class UnsupportedDgcVersionException(message: String = "Wrong Certificate Version") :
    DgcDecodeException(message)

public open class DgcDecodeException(message: String) : IllegalArgumentException(message)
