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
public data class TicketingPublicKeyJwkRemote(
    val x5c: List<String>,
    val kid: String,
    val alg: String,
    val use: String,
) : Parcelable
