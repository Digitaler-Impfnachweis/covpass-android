/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import COSE.CoseException
import COSE.OneKey
import COSE.Sign1Message
import android.util.Base64
import de.rki.covpass.sdk.cert.models.*
import de.rki.covpass.sdk.crypto.KeyIdentifier
import de.rki.covpass.sdk.dependencies.defaultCbor
import de.rki.covpass.sdk.utils.untagAll
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import java.security.GeneralSecurityException
import java.security.cert.X509Certificate
import java.time.Instant

/**
 * Compact representation of a [DscListEntry] with decoded raw data as [X509Certificate].
 */
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
public class CertValidator(trusted: Iterable<TrustedCert>, private val cbor: Cbor = defaultCbor) {

    private var state = CertValidatorState(trusted)

    private val vaccinationCertOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.2",
        "1.3.6.1.4.1.0.1847.2021.1.2"
    )
    private val testCertOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.1",
        "1.3.6.1.4.1.0.1847.2021.1.1"
    )
    private val recoveryCertOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.3",
        "1.3.6.1.4.1.0.1847.2021.1.3"
    )
    private val allCertOids = vaccinationCertOids + testCertOids + recoveryCertOids

    /** Finds trusted certificates matching the given [kid]. */
    private fun findByKid(kid: KeyIdentifier): List<TrustedCert> =
        state.kidToCerts[kid] ?: emptyList()

    /** Updates the trusted certificates. */
    public fun updateTrustedCerts(trusted: Iterable<TrustedCert>) {
        state = CertValidatorState(trusted)
    }

    internal fun decodeAndValidate(cwt: CBORWebToken, cert: X509Certificate): CovCertificate {
        val covCertificate = decodeCovCert(cwt)
        if (!cert.checkCertOid(covCertificate.dgcEntry)) {
            throw NoMatchingExtendedKeyUsageException()
        }
        return covCertificate.copy(
            issuer = cwt.issuer,
            validFrom = cwt.validFrom,
            validUntil = cwt.validUntil
        )
    }

    internal fun decodeCovCert(cwt: CBORWebToken): CovCertificate =
        cbor.decodeFromByteArray(
            cwt.rawCbor.untagAll()[HEALTH_CERTIFICATE_CLAIM][DIGITAL_GREEN_CERTIFICATE].EncodeToBytes()
        )

    /**
     * Decodes a [Sign1Message] and asserts that the [CBORWebToken] is valid.
     *
     * @returns The decoded [CovCertificate], if the validation was successful.
     *
     * @throws ExpiredCwtException If the [CBORWebToken] has expired.
     * @throws BadCoseSignatureException If the signature validation failed.
     */
    public fun decodeAndValidate(cose: Sign1Message): CovCertificate {
        val cwt = CBORWebToken.decode(cose.GetContent())
        if (cwt.validUntil.isBefore(Instant.now())) {
            throw ExpiredCwtException()
        }

        val kid = KeyIdentifier(
            cose.protectedAttributes?.get(4)?.GetByteString()?.sliceArray(0..7)
                ?: cose.unprotectedAttributes.get(4).GetByteString().sliceArray(0..7)
        )
        val certs = findByKid(kid).takeIf { it.isNotEmpty() }
            ?: state.trustedCerts.toList()
        for (cert in certs) {
            try {
                cert.certificate.checkValidity()
                // Validate the COSE signature
                if (cose.validate(OneKey(cert.certificate.publicKey, null))) {
                    return decodeAndValidate(cwt, cert.certificate)
                }
            } catch (e: CoseException) {
                continue
            } catch (e: GeneralSecurityException) {
                continue
            }
        }
        throw BadCoseSignatureException()
    }

    private fun X509Certificate.checkCertOid(dgcEntry: DGCEntry): Boolean {
        val extendedKeyUsageIntersect =
            if (extendedKeyUsage.isNullOrEmpty()) {
                emptySet<String>()
            } else {
                extendedKeyUsage.toSet() intersect allCertOids
            }
        return extendedKeyUsageIntersect.isEmpty() ||
            (
                when (dgcEntry) {
                    is Vaccination -> vaccinationCertOids
                    is Test -> testCertOids
                    else -> recoveryCertOids
                } intersect extendedKeyUsageIntersect
                ).isNotEmpty()
    }

    private companion object {
        private const val HEALTH_CERTIFICATE_CLAIM = -260
        private const val DIGITAL_GREEN_CERTIFICATE = 1
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

/** Thrown when the OID values does not match the extendedKeyUsage. */
public open class NoMatchingExtendedKeyUsageException(
    message: String = "Validation failed"
) : DgcDecodeException(message)
