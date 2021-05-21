/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.android.cert.models

import kotlinx.serialization.Serializable

@Serializable
public data class DscList(
    var certificates: List<DscListEntry>
)

@Serializable
public data class DscListEntry(
    val certificateType: String,
    val country: String,
    val kid: String,
    val rawData: String,
    val signature: String,
    val thumbprint: String,
    val timestamp: String
)
