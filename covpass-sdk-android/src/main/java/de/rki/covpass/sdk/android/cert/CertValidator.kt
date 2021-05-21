/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.cert

import COSE.CoseException
import COSE.OneKey
import COSE.Sign1Message
import android.util.Base64
import de.rki.covpass.sdk.android.cert.models.CBORWebToken
import de.rki.covpass.sdk.android.crypto.KeyIdentifier
import java.security.GeneralSecurityException
import java.security.cert.X509Certificate
import java.time.Instant

public data class TrustedCert(
    val country: String,
    val kid: String,
    val certificate: X509Certificate,
)

/**
 * A certificate validator which can validate and construct certificate paths.
 *
 * @constructor The constructor takes a set of trusted certificates.
 */
public class CertValidator(trusted: Iterable<TrustedCert>) {
    private var state = CertValidatorState(trusted)

    /** Finds trusted certificates matching the given [kid]. */
    private fun findByKid(kid: KeyIdentifier): List<TrustedCert> =
        state.kidToCerts[kid] ?: emptyList()

    /** Updates the trusted certificates. */
    public fun updateTrustedCerts(trusted: Iterable<TrustedCert>) {
        state = CertValidatorState(trusted)
    }

    public fun validate(cose: Sign1Message): CBORWebToken {
        val cwt = CBORWebToken.decode(cose.GetContent())
        val kid = KeyIdentifier(
            cose.protectedAttributes?.get(4)?.GetByteString()?.sliceArray(0..7)
                ?: cose.unprotectedAttributes.get(4).GetByteString().sliceArray(0..7)
        )

        if (cwt.validUntil.isBefore(Instant.now())) {
            throw ExpiredCwtException()
        }

        var certs = findByKid(kid)
        if (certs.isNullOrEmpty()) {
            certs = state.trustedCerts.toList()
        }
        for (cert in certs) {
            try {
                cert.certificate.checkValidity()
                // Validate the COSE signature and the country issuer
                if (cert.country == cwt.issuer && cose.validate(OneKey(cert.certificate.publicKey, null))) {
                    return cwt
                }
            } catch (e: CoseException) {
                continue
            } catch (e: GeneralSecurityException) {
                continue
            }
        }
        throw BadCoseSignatureException()
    }
}

private class CertValidatorState(trusted: Iterable<TrustedCert>) {
    val trustedCerts: Set<TrustedCert> = trusted.toSet()

    val kidToCerts by lazy { trustedCerts.groupBy { KeyIdentifier(Base64.decode(it.kid, Base64.DEFAULT)) } }
}

/** Thrown when the [CBORWebToken] expiry validation failed. */
public open class ExpiredCwtException(message: String = "Certificate expired") : DgcDecodeException(message)

/** Thrown when the COSE signature validation failed. */
public open class BadCoseSignatureException(message: String = "Validation failed") : DgcDecodeException(message)
