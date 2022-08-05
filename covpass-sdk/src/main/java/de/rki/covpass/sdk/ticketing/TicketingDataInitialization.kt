/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.ticketing

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
public data class TicketingDataInitialization(
    val protocol: String,
    val protocolVersion: String,
    val serviceIdentity: String,
    val privacyUrl: String,
    val token: String,
    val consent: String,
    val subject: String,
    val serviceProvider: String,
) : Parcelable
