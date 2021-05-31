/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import COSE.CoseException
import COSE.Sign1Message
import de.rki.covpass.base45.Base45
import de.rki.covpass.sdk.cert.models.CBORWebToken
import de.rki.covpass.sdk.cert.models.Name
import de.rki.covpass.sdk.cert.models.Test
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.cert.models.Recovery
import de.rki.covpass.sdk.utils.Zlib
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.security.GeneralSecurityException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
     * Converts a [qrContent] to a [CovCertificate] data model.
     *
     * @throws ExpiredCwtException If the [CBORWebToken] has expired.
     * @throws BadCoseSignatureException If the signature validation failed.
     * @throws UnsupportedDgcVersionException If the Digital Green Certificate version is unsupported.
     * @throws CoseException For generic COSE errors.
     * @throws GeneralSecurityException For generic cryptography errors.
     */
    public fun decodeVaccinationCert(qrContent: String): CovCertificate {
        val cwt = decodeCWT(qrContent)
        var cert: CovCertificate =
            cbor.decodeFromByteArray(cwt.rawCbor[HEALTH_CERTIFICATE_CLAIM][DIGITAL_GREEN_CERTIFICATE].EncodeToBytes())

        // FIXME remove this mock code when we have real QR codes for tests and recoveries
        val mock = true
        if (mock) {
            when (LocalDateTime.now().second / 10) {
                0 -> cert = CovCertificate(
                    name = Name(givenName = "Vorname1", familyName = "Nachname1"),
                    birthDate = LocalDate.of(2020, 12, 24),
                    tests = listOf(Test(id = (0..9999).random().toString())),
                    version = "1.0.0"
                )
                1 -> cert = CovCertificate(
                    name = Name(givenName = "Vorname2", familyName = "Nachname2"),
                    birthDate = LocalDate.of(2020, 12, 24),
                    tests = listOf(Test(id = (0..9999).random().toString())),
                    version = "1.0.0"
                )
                2 -> cert = CovCertificate(
                    name = Name(givenName = "Vorname1", familyName = "Nachname1"),
                    birthDate = LocalDate.of(2020, 12, 24),
                    recoveries = listOf(Recovery(id = (0..9999).random().toString())),
                    version = "1.0.0"
                )
                3 -> cert = CovCertificate(
                    name = Name(givenName = "Vorname2", familyName = "Nachname2"),
                    birthDate = LocalDate.of(2020, 12, 24),
                    recoveries = listOf(Recovery(id = (0..9999).random().toString())),
                    version = "1.0.0"
                )
            }
        }

        if (cert.version != "1.0.0") {
            throw UnsupportedDgcVersionException()
        }
        return cert.copy(issuer = cwt.issuer, validFrom = cwt.validFrom, validUntil = cwt.validUntil)
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
