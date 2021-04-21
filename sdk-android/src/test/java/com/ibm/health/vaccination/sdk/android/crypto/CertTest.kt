package com.ibm.health.vaccination.sdk.android.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import org.junit.Ignore
import org.junit.Test

internal class CertTest {
    private val sealCert = readPem(readTextFile("seal-cert.pem")).first()
    private val caCert = readPem(readTextFile("intermediate-cert.pem")).first()
    private val rootCert = readPem(readTextFile("root-cert.pem")).first()

    private val PEM = listOf(
        readTextFile("seal-cert.pem"),
        readTextFile("intermediate-cert.pem"),
        readTextFile("root-cert.pem"),
    ).joinToString("\n\n")

    private val certs = readPem(PEM).toSet()
    private val validator = CertValidator(certs)

    private fun readTextFile(path: String): String =
        String(javaClass.classLoader.getResourceAsStream(path).readBytes()).trim('\r', '\n')

    @Test
    fun `read multiple certs`() {
        assertThat(certs).isEqualTo(setOf(sealCert, caCert, rootCert))
    }

    @Test
    fun `extract CA`() {
        assertThat(validator.rootCerts).isEqualTo(setOf(rootCert))
    }

    @Test
    fun `extract anchors`() {
        assertThat(validator.trustAnchors.map { it.trustedCert }.toSet()).isEqualTo(setOf(rootCert))
    }

    @Ignore("Bouncy Castle fails to validate the rsassaPss signatures, but Conscrypt and OpenSSL succeed.")
    @Test
    fun `validate cert`() {
        assertThat { validator.validate(sealCert) }.isSuccess()
    }
}
