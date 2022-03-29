package de.rki.covpass.sdk.crypto

import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class AESTest {
    private val key = SecretKeySpec("12345678901234567890123456789012".encodeToByteArray(), "AES")
    private val data = "hello"

    @Test
    fun encryptionFixedIV() {
        // Let's try with a 16 bytes IV instead of the default 12
        val iv = "1234567890123456".encodeToByteArray()
        val encrypted = data.encodeToByteArray().encryptAesGcm(key, iv)
        assertEquals(data.length + AES_GCM_TAG_BYTES, encrypted.size)
        assertEquals(data, encrypted.decryptAesGcm(key, iv).decodeToString())
        assertContentEquals(
            byteArrayOf(
                0x3e,
                0xd7.toByte(),
                0xe4.toByte(),
                0xc7.toByte(),
                0xac.toByte(),
                0x7e,
                0x4d,
                0x7a,
                0x25,
                0x98.toByte(),
                0x39,
                0x17,
                0x5c,
                0xf6.toByte(),
                0xb5.toByte(),
                0xea.toByte(),
                0x51,
                0x52,
                0x7a,
                0x31,
                0x98.toByte(),
            ),
            encrypted,
        )
    }
}
