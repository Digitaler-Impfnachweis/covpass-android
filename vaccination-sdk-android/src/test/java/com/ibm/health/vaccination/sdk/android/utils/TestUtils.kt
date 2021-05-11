package com.ibm.health.vaccination.sdk.android.utils

import com.ibm.health.vaccination.sdk.android.crypto.CertValidator
import com.ibm.health.vaccination.sdk.android.crypto.readPem

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
