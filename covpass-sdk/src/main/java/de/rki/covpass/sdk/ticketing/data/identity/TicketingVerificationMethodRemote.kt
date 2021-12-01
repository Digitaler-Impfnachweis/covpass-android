/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing.data.identity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
public data class TicketingVerificationMethodRemote(
    val id: String,
    val type: String,
    val controller: String,
    val publicKeyJwk: TicketingPublicKeyJwkRemote? = null,
    val verificationMethods: List<String>? = null,
) : Parcelable
