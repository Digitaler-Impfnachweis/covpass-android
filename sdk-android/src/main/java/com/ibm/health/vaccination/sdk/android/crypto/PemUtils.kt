package com.ibm.health.vaccination.sdk.android.crypto

import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/** Reads a PEM file and returns a list of the [X509Certificate]s contained in that file. */
public fun readPem(data: String): List<X509Certificate> =
    readPem(data.byteInputStream())

/** Reads a PEM file and returns a list of the [X509Certificate]s contained in that file. */
public fun readPem(data: ByteArray): List<X509Certificate> =
    readPem(data.inputStream())

/** Reads a PEM file and returns a list of the [X509Certificate]s contained in that file. */
public fun readPem(data: InputStream): List<X509Certificate> {
    // XXX: Conscrypt handles the PEM format only if there are no empty lines before or between the certs. Strip them.
    var buffered = String(data.readBytes()).trim().replace(newlineRegex, "\n").byteInputStream().buffered()
    val certificateFactory = CertificateFactory.getInstance("X.509")
    return certificateFactory.generateCertificates(buffered).mapNotNull { it as? X509Certificate }
}

private val newlineRegex = Regex("\n\n+")
