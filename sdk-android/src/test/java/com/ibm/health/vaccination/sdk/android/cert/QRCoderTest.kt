package com.ibm.health.vaccination.sdk.android.cert

import COSE.OneKey
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.readPem
import com.ibm.health.vaccination.sdk.android.utils.readResource
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Test
import java.security.Security

internal class QRCoderTest {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    val sealCert by lazy { readPem(readResource("seal-cert.pem")).first() }
    val data by lazy { readResource("vaccination-cert.txt").replace("\n", "") }
    val validator by lazy { CertValidator(listOf(sealCert)) }
    val qrCoder by lazy { QRCoder(validator) }

    @Test
    fun `validate vaccination`() {
        val cose = qrCoder.decodeCose(data)
        assertThat(cose.validate(OneKey(sealCert.publicKey, null))).isTrue()
    }

    /**
     * Makes sure we have a correct decoding a data from some QR code to the ValidationCertificate.
     */
    @Test
    fun `check data fields after decoding`() {
        val qrDataModel = qrCoder.decodeVaccinationCert(data)
        assertThat(qrDataModel).isNotEqualTo(null)
        assertThat(qrDataModel.fullName).isEqualTo(TEST_NAME_IN_QR_DATA)
        assertThat(qrDataModel.vaccinations[0].country).isEqualTo(TEST_COUNTRY_IN_QR_DATA)
    }

    companion object {
        val TEST_NAME_IN_QR_DATA: String = "Erika Dörte Schmitt Mustermann"
        val TEST_COUNTRY_IN_QR_DATA: String = "DE"
    }
}
