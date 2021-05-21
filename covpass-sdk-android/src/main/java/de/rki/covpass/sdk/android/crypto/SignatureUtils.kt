/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.crypto

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException

public fun validateSignature(key: PublicKey, data: ByteArray, signature: ByteArray, algorithm: String) {
    val javaSignature = if (algorithm.endsWith("withECDSA")) {
        val (r, s) = signature.splitHalves()
        DERSequence(
            arrayOf(
                ASN1Integer(BigInteger(1, r)),
                ASN1Integer(BigInteger(1, s)),
            )
        ).encoded
    } else {
        signature
    }
    val verifier = Signature.getInstance(algorithm, BouncyCastleProvider()).apply {
        initVerify(key)
        update(data)
    }
    if (!verifier.verify(javaSignature)) {
        throw SignatureException()
    }
}

private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
    take(size / 2).toByteArray() to drop(size / 2).toByteArray()
