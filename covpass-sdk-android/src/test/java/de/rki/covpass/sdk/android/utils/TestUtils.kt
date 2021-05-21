/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.utils

import de.rki.covpass.sdk.android.crypto.CertValidator
import de.rki.covpass.sdk.android.crypto.readPem

internal fun Any.readResource(path: String): String =
    String(javaClass.classLoader!!.getResourceAsStream(path).readBytes())

internal fun Any.loadCAValidator(): CertValidator {
    val pem = listOf(
        readResource("seal-cert.pem"),
        readResource("intermediate-cert.pem"),
        readResource("root-cert.pem"),
    ).joinToString("\n\n")
    return CertValidator(readPem(pem).toSet())
}
