package com.ibm.health.vaccination.sdk.android.crypto

import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate

/** Whether this is a root certificate. */
public val X509Certificate.isRoot: Boolean get() = subjectX500Principal == issuerX500Principal

/** Extracts only the root certs from the given certificates. */
public fun Iterable<X509Certificate>.getRootCerts(): Set<X509Certificate> =
    filter { it.isRoot }.toSet()

/** Converts the given certificates to [TrustAnchor]s. */
public fun Iterable<X509Certificate>.toTrustAnchors(): Set<TrustAnchor> =
    map { TrustAnchor(it, null) }.toSet()

/** Provides useful X509 extension Oids. */
public enum class X509ExtensionOid(public val oid: String) {
    AUTHORITY_KEY_IDENTIFIER("2.5.29.35"),
    SUBJECT_KEY_IDENTIFIER("2.5.29.14"),
}

/** Gets an extension value based on [X509ExtensionOid]. */
public fun X509Certificate.getExtensionValue(extension: X509ExtensionOid): ByteArray? =
    getExtensionValue(extension.oid)
