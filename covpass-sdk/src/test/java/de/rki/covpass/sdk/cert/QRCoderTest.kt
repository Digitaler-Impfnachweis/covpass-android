/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import COSE.OneKey
import de.rki.covpass.base45.Base45DecodeException
import de.rki.covpass.sdk.cert.models.CBORWebToken
import de.rki.covpass.sdk.crypto.readPem
import de.rki.covpass.sdk.dependencies.defaultCbor
import de.rki.covpass.sdk.ticketing.TicketingDataInitialization
import de.rki.covpass.sdk.utils.BaseSdkTest
import de.rki.covpass.sdk.utils.readResource
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class QRCoderTest : BaseSdkTest() {
    val sealCert by lazy { readPem(readResource("seal-cert.pem")).first() }
    val data by lazy { readResource("vaccination-cert.txt").replace("\n", "") }
    val validator by lazy {
        CertValidator(
            listOf(TrustedCert(country = "DE", kid = "asdf", certificate = sealCert)),
            defaultCbor,
        )
    }
    val qrCoder by lazy { QRCoder(validator) }

    @Test
    fun `validate cert`() {
        val cose = qrCoder.decodeCose(data)
        assertTrue(cose.validate(OneKey(sealCert.publicKey, null)))
    }

    @Test
    fun `expired certificate`() {
        assertFailsWith<ExpiredCwtException> { qrCoder.decodeCovCert(data) }
    }

    @Test
    fun `decode broken certificate`() {
        assertFailsWith<DgcDecodeException> {
            qrCoder.decodeCose(data.slice(0 until data.length - 3))
        }
        assertFailsWith<Base45DecodeException> {
            qrCoder.decodeCose(data.slice(0 until data.length - 2))
        }
    }

    @Test
    fun `decode certificate`() {
        val cose = qrCoder.decodeCose(data)
        // Skip seal validation and only decode + validate the QR data itself
        validator.decodeAndValidate(CBORWebToken.decode(cose.GetContent()), sealCert)
    }

    @Test
    fun `decode ticketing qr code`() {
        val ticketingFromfile =
            qrCoder.validateTicketing(readResource("ticketing_qr_code.txt").replace("\n", ""))
        val ticketingDataInitialization =
            TicketingDataInitialization(
                "DCCVALIDATION",
                "1.0.0",
                "http://dccexampleprovider/dcc/identity",
                "https://myprivacy",
                "eyJhbGciOiJFUzI1NiIsImtpZCI6IjIzNDMifQ.eyJpc3MiOiJodHRwczovL3NlcnZpY2Vwcm92aWRlciIsInN1Yi" +
                    "I6IkFERURERERERERERERERERERCIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.Gdnw1LF1BccpYKcTA" +
                    "uyNL_KeY1Z0juz9WU9660BedgRzrZplxUZRjr09JIlZNtgZtqgAZ9Pma3kgPhUln9Gufw",
                "I want to check your DCC to confirm your booking!:)",
                "Booking Nr. ?",
                "Service Provider.com",
            )
        assertEquals(ticketingDataInitialization, ticketingFromfile)
    }

    @Test
    fun `decode wrong ticketing qr code`() {
        assertFailsWith(WrongTicketingProtocolException::class) {
            qrCoder.validateTicketing(readResource("ticketing_wrong_qr_code.txt").replace("\n", ""))
        }
    }

    @Test
    fun `decode vaccination certificate with validateTicketing`() {
        assertFailsWith(SerializationException::class) {
            qrCoder.validateTicketing(data)
        }
    }
}
