/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.cert.models

import kotlinx.serialization.Serializable

/**
 * List of [DscListEntry].
 */
@Serializable
public data class DscList(
    var certificates: List<DscListEntry>
)

/**
 * Represents a Document Signer Certificate as raw data.
 */
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
