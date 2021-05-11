package com.ibm.health.vaccination.sdk.android.crypto

import java.security.GeneralSecurityException
import java.security.cert.*
import javax.security.auth.x500.X500Principal

/**
 * A certificate validator which can validate and construct certificate paths.
 *
 * @constructor The constructor takes a set of trusted certificates.
 */
public class CertValidator(trusted: Iterable<X509Certificate>) {
    /** The trusted certificates. */
    public val trustedCerts: Set<X509Certificate> = trusted.toSet()

    /** The root certificates contained within [trustedCerts]. */
    public val rootCerts: Set<X509Certificate> by lazy { trustedCerts.getRootCerts() }

    /** The [TrustAnchor]s contained within [rootCerts]. */
    public val trustAnchors: Set<TrustAnchor> by lazy { rootCerts.toTrustAnchors() }

    private val subjectToCerts by lazy { trustedCerts.groupBy { it.subjectX500Principal } }

    /**
     * Returns the certificate path from [leaf] to one of the [rootCerts].
     *
     * @throws InvalidCertPathException if no valid cert path could be constructed.
     * @throws GeneralSecurityException if validation of the path failed.
     */
    public fun getCertPath(leaf: X509Certificate): List<X509Certificate> {
        val path = mutableListOf<X509Certificate>()
        var cert = leaf
        while (true) {
            cert.checkValidity()
            path.add(cert)
            if (cert in rootCerts) {
                return path
            }
            cert = findParent(cert) ?: throw InvalidCertPathException()
        }
    }

    /**
     * Validates the given [leaf] certificate against the [trustedCerts].
     *
     * @throws GeneralSecurityException if the certificate couldn't be validated.
     */
    public fun validate(leaf: X509Certificate): PKIXCertPathValidatorResult {
        val pkixParameters = PKIXParameters(trustAnchors).apply {
            isRevocationEnabled = false
        }
        val validator = CertPathValidator.getInstance("PKIX")
        val certPath = CertificateFactory.getInstance("X.509").generateCertPath(getCertPath(leaf))
        return validator.validate(certPath, pkixParameters) as? PKIXCertPathValidatorResult
            ?: throw InvalidCertPathException()
    }

    /** Finds trusted certificates matching the given [subject]. */
    public fun findBySubject(subject: X500Principal): List<X509Certificate> =
        subjectToCerts[subject] ?: emptyList()

    /** Returns the parent of the given certificate or `null` if no certificate matches. */
    private fun findParent(cert: X509Certificate): X509Certificate? =
        findBySubject(cert.issuerX500Principal).find {
            cert != it &&
                try {
                    cert.verify(it.publicKey)
                    true
                } catch (e: GeneralSecurityException) {
                    false
                }
        }
}

/** Thrown when no valid certificate path could be constructed. */
public class InvalidCertPathException : GeneralSecurityException()
