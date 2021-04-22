package com.ibm.health.vaccination.sdk.android.cose

import COSE.*
import assertk.assertThat
import assertk.assertions.*
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

@RunWith(RobolectricTestRunner::class)
internal class CoseSign1Test {
    private val content = byteArrayOf(12, 1, 4)

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
