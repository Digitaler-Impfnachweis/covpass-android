/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import COSE.CoseException
import COSE.Sign1Message
import de.rki.covpass.base45.Base45
import de.rki.covpass.sdk.cert.models.CBORWebToken
import de.rki.covpass.sdk.cert.models.CovCertificate
import de.rki.covpass.sdk.dependencies.defaultJson
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.utils.Zlib
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Used to encode/decode QR code string.
 */
public class QRCoder(private val validator: CertValidator) {

    /** Returns the raw COSE ByteArray contained within the certificate. */
    internal fun decodeRawCose(qr: String): ByteArray {
        val qrContent = qr.removePrefix("HC1:").toByteArray()
        try {
            return Zlib.decompress(Base45.decode(qrContent))
        } catch (e: IOException) {
            throw DgcDecodeException("Not a valid zlib compressed DCC")
        }
    }

    internal fun decodeCose(qr: String): Sign1Message =
        Sign1Message.DecodeFromBytes(decodeRawCose(qr)) as? Sign1Message
            ?: throw CoseException("Not a cose-sign1 message")

    /**
     * Converts a [qrContent] to a [CovCertificate] data model.
     *
     * @throws ExpiredCwtException If the [CBORWebToken] has expired.
     * @throws BadCoseSignatureException If the signature validation failed.
     * @throws CoseException For generic COSE errors.
     * @throws GeneralSecurityException For generic cryptography errors.
     */
    public fun decodeCovCert(qrContent: String): CovCertificate =
        validator.decodeAndValidate(decodeCose(qrContent))

    public fun validateTicketing(qrContent: String): TicketingDataInitialization {
        val ticketingData = defaultJson.decodeFromString<TicketingDataInitialization>(qrContent)
        if (ticketingData.protocol == TICKETING_PROTOCOL) {
            return ticketingData
        }
        throw WrongTicketingProtocolException("Wrong Ticketing Protocol")
    }

    private companion object {
        const val TICKETING_PROTOCOL = "DCCVALIDATION"
    }
}

/** Thrown when the decoding of a Document Signer Certificate fails. */
public open class DgcDecodeException(message: String) : IllegalArgumentException(message)

/** Thrown when the decoding of a Ticketing Data fails. */
public open class WrongTicketingProtocolException(message: String) : IllegalArgumentException(message)
