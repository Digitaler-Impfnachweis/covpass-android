/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.encoding

import android.util.Base64
import java.security.PrivateKey
import java.security.Signature

public class TicketingDgcSigner {
    public fun signDcc(data: ByteArray, privateKey: PrivateKey): String {
        val signature: Signature = Signature.getInstance(SIG_ALG)
        signature.initSign(privateKey)
        signature.update(data)
        return Base64.encodeToString(signature.sign(), Base64.NO_WRAP)
    }

    private companion object {
        const val SIG_ALG = "SHA256withECDSA"
    }
}
