/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.vaccination.sdk.android.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import com.ibm.health.vaccination.sdk.android.utils.loadCAValidator
import com.ibm.health.vaccination.sdk.android.utils.readResource
import org.junit.Ignore
import org.junit.Test

internal class CertTest {
    private val sealCert = readPem(readResource("seal-cert.pem")).first()
    private val caCert = readPem(readResource("intermediate-cert.pem")).first()
    private val rootCert = readPem(readResource("root-cert.pem")).first()

    private val validator = loadCAValidator()

    @Test
    fun `read multiple certs`() {
        assertThat(validator.trustedCerts).isEqualTo(setOf(sealCert, caCert, rootCert))
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
