package de.rki.covpass.sdk.crypto

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private fun SecretKey.aesGcmCipher(mode: Int, iv: ByteArray, tagLength: Int = AES_GCM_TAG_BITS): Cipher =
    Cipher.getInstance("AES/GCM/NoPadding").apply {
        init(mode, this@aesGcmCipher, GCMParameterSpec(tagLength, iv))
    }

/** Encrypts this [ByteArray] with AES-GCM using the given [key] and [iv]. The result won't be prefixed with the IV. */
public fun ByteArray.encryptAesGcm(key: SecretKey, iv: ByteArray): ByteArray {
    return key.aesGcmCipher(Cipher.ENCRYPT_MODE, iv).doFinal(this)
}

/** Decrypts this [ByteArray] with AES-GCM using the given [key] and [iv]. */
public fun ByteArray.decryptAesGcm(key: SecretKey, iv: ByteArray): ByteArray =
    key.aesGcmCipher(Cipher.DECRYPT_MODE, iv).doFinal(this)

public const val AES_GCM_TAG_BITS: Int = 128
public const val AES_GCM_TAG_BYTES: Int = AES_GCM_TAG_BITS / 8
