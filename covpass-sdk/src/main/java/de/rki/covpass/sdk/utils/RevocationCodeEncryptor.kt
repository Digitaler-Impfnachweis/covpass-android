/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import java.security.PublicKey
import javax.crypto.Cipher

public class RevocationCodeEncryptor(private val publicKey: PublicKey) {

    public fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance("ECIESwithSHA256")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data.toByteArray())
    }
}
