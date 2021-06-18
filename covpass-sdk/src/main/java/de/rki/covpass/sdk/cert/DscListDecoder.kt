/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert

import de.rki.covpass.sdk.cert.models.DscList
import de.rki.covpass.sdk.crypto.validateSignature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bouncycastle.util.encoders.Base64
import java.security.PublicKey
import java.security.SignatureException

/**
 * Decoder for decoding the results from [DscListService].
 */
public class DscListDecoder(private val publicKey: PublicKey) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Decodes the given data string to a [DscList].
     * Validates the signature.
     *
     * @throws SignatureException if signature validation fails.
     */
    public fun decodeDscList(data: String): DscList {
        val encodedSignature = data.substringBefore("{")
        val signature = Base64.decode(encodedSignature)
        val trustedList = data.substring(encodedSignature.length).trim()
        validateSignature(
            key = publicKey,
            data = trustedList.toByteArray(),
            signature = signature,
            algorithm = "SHA256withECDSA",
        )
        return json.decodeFromString(trustedList)
    }
}
