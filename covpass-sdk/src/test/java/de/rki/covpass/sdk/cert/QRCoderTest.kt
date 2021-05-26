/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import COSE.OneKey
import assertk.assertThat
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import de.rki.covpass.sdk.crypto.readPem
import de.rki.covpass.sdk.utils.BaseSdkTest
import de.rki.covpass.sdk.utils.readResource
import org.junit.Test

internal class QRCoderTest : BaseSdkTest() {
    val sealCert by lazy { readPem(readResource("seal-cert.pem")).first() }
    val data by lazy { readResource("vaccination-cert.txt").replace("\n", "") }
    val validator by lazy { CertValidator(listOf(TrustedCert(country = "DE", kid = "asdf", certificate = sealCert))) }
    val qrCoder by lazy { QRCoder(validator) }

    @Test
    fun `validate vaccination`() {
        val cose = qrCoder.decodeCose(data)
        assertThat(cose.validate(OneKey(sealCert.publicKey, null))).isTrue()
    }

    @Test
    fun `expired certificate`() {
        assertThat {
            qrCoder.decodeVaccinationCert(data)
        }.isFailure().isInstanceOf(ExpiredCwtException::class)
    }
}
