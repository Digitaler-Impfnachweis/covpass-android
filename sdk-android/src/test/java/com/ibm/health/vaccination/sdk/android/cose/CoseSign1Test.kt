package com.ibm.health.vaccination.sdk.android.cose

import COSE.*
import assertk.assertThat
import assertk.assertions.*
import com.ibm.health.vaccination.sdk.android.crypto.readPem
import com.ibm.health.vaccination.sdk.android.qr.QRCoder
import com.ibm.health.vaccination.sdk.android.utils.fromHex
import com.ibm.health.vaccination.sdk.android.utils.loadCAValidator
import com.ibm.health.vaccination.sdk.android.utils.readResource
import com.upokecenter.cbor.CBORObject
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Ignore
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.security.spec.ECGenParameterSpec

internal class CoseSign1Test {
    private val content = byteArrayOf(12, 1, 4)

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun `CWT validation cross-check against RFC 8392 signed CWT example A3`() {
        val cwt = readResource("rfc-8392-signed-cwt-example-a3.txt").fromHex()

        // Test with COSE-JAVA
        val key = OneKey(CBORObject.DecodeFromBytes(readResource("rfc-8392-cwt-public-key.txt").fromHex()))
        val msg = Sign1Message.DecodeFromBytes(cwt) as Sign1Message
        msg.validate(key)

        // Test with our implementation
        val cose = CoseSign1.fromByteArray(cwt)
        assertThat { cose.validate(key.AsPublicKey()) }.isSuccess()
    }

    @Ignore("The cert probably isn't correct yet.")
    @Test
    fun `validate vaccination`() {
        val sealCert = readPem(readResource("seal-cert.pem")).first()
        val data = readResource("vaccination-cert.txt").replace("\n", "")
        val validator = loadCAValidator()

        // Cross-check against COSE-JAVA
        val rawCose = QRCoder().decodeRawCose(data)
        val msg = Sign1Message.DecodeFromBytes(rawCose) as Sign1Message
        assertThat(msg.validate(OneKey(sealCert.publicKey, null))).isTrue()

        val cose = QRCoder().decodeCose(data)
        assertThat(cose.signatureAlgorithm).isEqualTo(CoseSignatureAlgorithm.ECDSA_256)
        assertThat { cose.validate(sealCert) }.isSuccess()
        assertThat { cose.validate(validator) }.isSuccess()
    }

    @Test
    fun `COSE ECDSA validation cross-check against COSE-JAVA`() {
        for ((curve, algorithm) in ecdsaAlgorithms) {
            val keyPair = genEcdsa(curve)
            val raw = createMessage(keyPair, algorithm)

            val cose = CoseSign1.fromByteArray(raw)
            assertThat(cose.payload).isEqualTo(content)

            assertThat { cose.validate(genEcdsa(curve).public) }
                .isFailure().isInstanceOf(CoseValidationException::class)
            assertThat { cose.validate(genEcdsa(ecdsaAlgorithms.keys.first { it != curve }).public) }
                .isFailure().isInstanceOf(CoseValidationException::class)
            assertThat { cose.validate(keyPair.public) }.isSuccess()
        }
    }

    @Ignore("Conscrypt and COSE-JAVA don't support EdDSA")
    @Test
    fun `COSE EdDSA validation cross-check against COSE-JAVA`() {
        val keyPair = genEdDsa()
        val raw = createMessage(keyPair, AlgorithmID.EDDSA)

        val cose = CoseSign1.fromByteArray(raw)
        assertThat { cose.validate(genEdDsa().public) }
            .isFailure().isInstanceOf(CoseValidationException::class)
        assertThat { cose.validate(keyPair.public) }.isSuccess()
    }

    private fun genEcdsa(curve: String): KeyPair =
        KeyPairGenerator.getInstance("EC", "BC").apply {
            initialize(ECGenParameterSpec(curve))
        }.genKeyPair()

    private fun genEdDsa(): KeyPair =
        KeyPairGenerator.getInstance("Ed25519").genKeyPair()

    private fun createMessage(keyPair: KeyPair, algorithmID: AlgorithmID): ByteArray =
        Sign1Message().apply {
            addAttribute(HeaderKeys.Algorithm, algorithmID.AsCBOR(), Attribute.PROTECTED)
            SetContent(content)
            sign(OneKey(keyPair.public, keyPair.private))
        }.EncodeToBytes()

    companion object {
        val ecdsaAlgorithms = mapOf(
            "P-256" to AlgorithmID.ECDSA_256,
            "P-384" to AlgorithmID.ECDSA_384,
            "P-521" to AlgorithmID.ECDSA_512,
        )
    }
}
