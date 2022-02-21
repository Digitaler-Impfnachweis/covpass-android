/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
public data class TicketingValidationServiceIdentityResponse(
    val id: String,
    @SerialName("verificationMethod")
    val verificationMethods: List<TicketingVerificationMethodRemote>,
) : Parcelable {
    public fun getEncryptionPublicKey(): TicketingPublicKeyJwkRemote {
        return verificationMethods.first { it.publicKeyJwk?.use == "enc" }.publicKeyJwk ?: throw IllegalStateException()
    }
}
