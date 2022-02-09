/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.rules.eu

import kotlinx.serialization.Serializable

@Serializable
public data class CovPassDescriptionRemote(
    val lang: String,
    val desc: String
)
