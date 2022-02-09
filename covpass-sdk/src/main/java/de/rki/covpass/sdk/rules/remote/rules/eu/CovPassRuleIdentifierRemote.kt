/*
 * (C) Copyright IBM Deutschland GmbH 2021
 * (C) Copyright IBM Corp. 2021
 */

package de.rki.covpass.sdk.rules.remote.rules.eu

import kotlinx.serialization.Serializable

@Serializable
public data class CovPassRuleIdentifierRemote(
    val identifier: String,
    val version: String,
    val country: String,
    val hash: String
)
