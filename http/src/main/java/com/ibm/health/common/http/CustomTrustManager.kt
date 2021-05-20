/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package com.ibm.health.common.http

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.X509TrustManager

internal class CustomTrustManager(private val delegate: X509TrustManager) : X509TrustManager {
    private val delegateCheckServerTrusted = try {
        delegate::class.java.getMethod(
            "checkServerTrusted",
            Array<X509Certificate>::class.java,
            String::class.java,
            String::class.java
        )
    } catch (e: NoSuchMethodException) {
        null
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        delegate.acceptedIssuers

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String?) {
        delegate.checkClientTrusted(chain, authType)
        checkChain(chain)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String?) {
        delegate.checkServerTrusted(chain, authType)
        checkChain(chain)
    }

    // XXX: Required by Android
    fun checkServerTrusted(
        chain: Array<out X509Certificate>,
        authType: String?,
        host: String?
    ): List<X509Certificate>? {
        val result: List<X509Certificate>? = if (delegateCheckServerTrusted != null) {
            @Suppress("UNCHECKED_CAST")
            delegateCheckServerTrusted.invoke(delegate, chain, authType, host) as? List<X509Certificate>
        } else {
            delegate.checkServerTrusted(chain, authType)
            chain.toList()
        }
        checkChain(chain)
        return result
    }

    private fun checkChain(chain: Array<out X509Certificate>) {
        for (cert in chain) {
            checkCert(cert)
        }
    }

    private fun checkCert(cert: X509Certificate) {
        checkKeyLength(cert)
        checkSignatureAlgorithm(cert)
    }

    private fun checkKeyLength(cert: X509Certificate) {
        when (val publicKey = cert.publicKey) {
            is RSAPublicKey -> {
                if (publicKey.modulus.bitLength() < MIN_RSA_MODULUS_LEN_BITS) {
                    throw CertificateException("RSA modulus is < $MIN_RSA_MODULUS_LEN_BITS bits")
                }
            }
            is ECPublicKey -> {
                if (publicKey.params.curve.field.fieldSize < MIN_EC_FIELD_SIZE_BITS) {
                    throw CertificateException("EC key field size is < $MIN_EC_FIELD_SIZE_BITS bits")
                }
            }
            else -> throw CertificateException("Rejecting unknown key class ${publicKey::class.java.name}")
        }
    }

    private fun checkSignatureAlgorithm(cert: X509Certificate) {
        if (cert.sigAlgOID !in signatureWhitelist) {
            throw CertificateException("Signature uses an insecure hash function: ${cert.sigAlgOID}")
        }
    }

    private companion object {
        private const val MIN_RSA_MODULUS_LEN_BITS = 2048
        private const val MIN_EC_FIELD_SIZE_BITS = 256

        private val signatureWhitelist = listOf(
            "1.2.840.113549.1.1.11", // sha256WithRSAEncryption
            "1.2.840.113549.1.1.12", // sha384WithRSAEncryption
            "1.2.840.113549.1.1.13", // sha512WithRSAEncryption
            "1.2.840.10045.4.3.2", // ecdsa-with-SHA256
            "1.2.840.10045.4.3.3", // ecdsa-with-SHA384
            "1.2.840.10045.4.3.4", // ecdsa-with-SHA512
        )
    }
}
