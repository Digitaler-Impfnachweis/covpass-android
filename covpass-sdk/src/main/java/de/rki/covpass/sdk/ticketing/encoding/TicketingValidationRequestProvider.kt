/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.encoding

import android.util.Base64
import de.rki.covpass.sdk.ticketing.data.validate.TicketingValidationRequest
import java.security.PrivateKey
import java.security.PublicKey

public class TicketingValidationRequestProvider(
    private val ticketingDgcCryptor: TicketingDgcCryptor,
    private val ticketingDgcSigner: TicketingDgcSigner,
) {
    public fun provideTicketValidationRequest(
        dgcQrString: String,
        kid: String,
        publicKey: PublicKey,
        base64EncodedIv: String,
        privateKey: PrivateKey,
    ): TicketingValidationRequest {
        val iv = Base64.decode(base64EncodedIv, Base64.NO_WRAP)
        val ticketingEncryptedDgcData: TicketingEncryptedDgcData =
            ticketingDgcCryptor.encodeDcc(dgcQrString, iv, publicKey)
        val dcc = Base64.encodeToString(ticketingEncryptedDgcData.dataEncrypted, Base64.NO_WRAP)
        val encKey = Base64.encodeToString(ticketingEncryptedDgcData.encKey, Base64.NO_WRAP)
        val sig = ticketingDgcSigner.signDcc(ticketingEncryptedDgcData.dataEncrypted, privateKey)
        return TicketingValidationRequest(
            kid = kid,
            dcc = dcc,
            sig = sig,
            encKey = encKey,
        )
    }
}
