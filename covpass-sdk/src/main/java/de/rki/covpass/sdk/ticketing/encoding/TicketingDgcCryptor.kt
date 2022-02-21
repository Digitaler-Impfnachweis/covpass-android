/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.encoding

import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

public class TicketingDgcCryptor {
    private val dccCryptService = RsaOaepWithSha256AesGcm()

    public fun encodeDcc(
        dcc: String,
        iv: ByteArray,
        publicKey: PublicKey,
    ): TicketingEncryptedDgcData = dccCryptService.encryptData(
        dcc.toByteArray(),
        publicKey, iv
    )
}

public data class TicketingEncryptedDgcData(
    val dataEncrypted: ByteArray,
    val encKey: ByteArray,
)

private class RsaOaepWithSha256AesGcm {
    fun encryptData(data: ByteArray, publicKey: PublicKey, iv: ByteArray): TicketingEncryptedDgcData {
        if (iv.size > 16 || iv.size < 16 || iv.size % 8 > 0) {
            throw InvalidKeySpecException()
        }
        val keyGen: KeyGenerator = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey: SecretKey = keyGen.generateKey()
        val gcmParameterSpec = GCMParameterSpec(iv.size * 8, iv)
        val cipher: Cipher = Cipher.getInstance(DATA_CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        val dataEncrypted = cipher.doFinal(data)

        val keyCipher: Cipher = Cipher.getInstance(KEY_CIPHER)
        val oaepParameterSpec = OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        )
        keyCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec)
        val secretKeyBytes: ByteArray = secretKey.encoded
        val encKey = keyCipher.doFinal(secretKeyBytes)
        return TicketingEncryptedDgcData(dataEncrypted, encKey)
    }

    private companion object {
        const val KEY_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
        const val DATA_CIPHER = "AES/GCM/NoPadding"
    }
}
