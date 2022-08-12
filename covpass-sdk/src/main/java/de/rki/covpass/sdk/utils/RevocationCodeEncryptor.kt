/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.utils

import de.rki.covpass.sdk.crypto.encryptAesGcm
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

public class RevocationCodeEncryptor(private val publicKey: PublicKey) {

    private fun generateSenderKey(): KeyPair =
        KeyPairGenerator.getInstance("ECDH").run {
            initialize(ECGenParameterSpec("secp256r1"))
            generateKeyPair()
        }

    private fun getSharedSecret(
        senderPrivateKey: PrivateKey,
        receiverPubKey: PublicKey,
    ): ByteArray =
        KeyAgreement.getInstance("ECDH").run {
            init(senderPrivateKey)
            doPhase(receiverPubKey, true)
            generateSecret()
        }

    private fun deriveAESKeyAndIV(
        sharedSecret: ByteArray,
        senderPubBytes: ByteArray,
    ): Pair<SecretKey, ByteArray> {
        val aesKeyAndIV = (sharedSecret + byteArrayOf(0x0, 0x0, 0x0, 0x1) + senderPubBytes).sha256()
        return Pair(
            SecretKeySpec(aesKeyAndIV.sliceArray(0..15), "AES"),
            aesKeyAndIV.sliceArray(16..31),
        )
    }

    public fun encrypt(code: String): ByteArray {
        val senderKeyPair = generateSenderKey()
        val senderPubBytes = senderKeyPair.public.encoded
        val sharedSecret = getSharedSecret(senderKeyPair.private, publicKey)
        val (aesKey, iv) = deriveAESKeyAndIV(sharedSecret, senderPubBytes)
        return senderPubBytes + code.encodeToByteArray().encryptAesGcm(aesKey, iv)
    }
}
