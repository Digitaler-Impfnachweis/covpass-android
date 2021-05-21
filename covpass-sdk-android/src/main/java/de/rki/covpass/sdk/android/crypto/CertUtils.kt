/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.crypto

import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate

/** Whether this is a root certificate. */
internal val X509Certificate.isRoot: Boolean get() = isCA && subjectX500Principal == issuerX500Principal

/** Whether the CA basic constraint is true. */
internal val X509Certificate.isCA: Boolean get() = basicConstraints >= 0

/** Extracts only the root certs from the given certificates. */
internal fun Iterable<X509Certificate>.getRootCerts(): Set<X509Certificate> =
    filter { it.isRoot }.toSet()

/** Converts the given certificates to [TrustAnchor]s. */
internal fun Iterable<X509Certificate>.toTrustAnchors(): Set<TrustAnchor> =
    map { TrustAnchor(it, null) }.toSet()
